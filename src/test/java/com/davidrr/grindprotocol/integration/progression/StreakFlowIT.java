package com.davidrr.grindprotocol.integration.progression;

import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.integration.AbstractPostgresIT;
import com.davidrr.grindprotocol.progression.service.StreakService;
import com.davidrr.grindprotocol.progression.service.DayClosingJob;
import com.davidrr.grindprotocol.task.dto.*;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.repository.*;
import com.davidrr.grindprotocol.task.service.*;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import com.davidrr.grindprotocol.userprofile.service.UserProfileService;
import com.davidrr.grindprotocol.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real transactions and Flyway on the isolated PostgreSQL Testcontainer. */
@AutoConfigureMockMvc
class StreakFlowIT extends AbstractPostgresIT {
    @Autowired MockMvc mvc;
    @MockitoBean(name = "businessClock") Clock clock;
    @Autowired TaskService tasks;
    @Autowired TaskTemplateService templates;
    @Autowired TaskCompletionService completions;
    @Autowired DailyProgressService progress;
    @Autowired StreakService streaks;
    @Autowired UserRepository users;
    @Autowired UserProfileRepository profiles;
    @Autowired UserProfileService profileService;
    @Autowired DailyProgressRepository daily;
    @Autowired TaskRepository taskRepository;
    @Autowired TaskTemplateRepository templateRepository;
    @Autowired TaskCompletionRepository completionRepository;
    @Autowired PlatformTransactionManager transactionManager;
    private final AtomicReference<Instant> instant = new AtomicReference<>();
    private static final ZoneId ZONE = ZoneId.of("Europe/Madrid");
    private static final LocalDate DAY = LocalDate.of(2030, 10, 26);
    private Long userId;
    private TransactionTemplate tx;

    @BeforeEach void setUp() {
        instant.set(DAY.atTime(12, 0).atZone(ZONE).toInstant());
        when(clock.getZone()).thenReturn(ZONE);
        when(clock.instant()).thenAnswer(i -> instant.get());
        tx = new TransactionTemplate(transactionManager);
        String name = "streak_" + UUID.randomUUID();
        userId = tx.execute(s -> users.save(User.builder().username(name).email(name + "@test.com")
                .password("unused").role("USER").enabled(true).build()).getId());
        profileService.createDefaultProfile(users.findById(userId).orElseThrow());
    }
    private void advance(int days) { instant.set(DAY.plusDays(days).atTime(12, 0).atZone(ZONE).toInstant()); }
    private long task(boolean mandatory) {
        CreateTaskRequest request = TestDataFactory.createTaskRequest();
        request.setTraitCodes(Set.of()); request.setMandatory(mandatory);
        return tasks.createTask(userId, request).getId();
    }
    private long fromTemplate() {
        CreateTaskTemplateRequest request = TestDataFactory.createTaskTemplateRequest();
        request.setTraitCodes(Set.of());
        long template = templates.createTemplate(userId, request).getId();
        return tasks.createTaskFromTemplate(userId, CreateTaskFromTemplateRequest.builder().templateId(template).build()).getId();
    }
    private void complete(long id) { completions.completeTask(userId, TestDataFactory.createTaskCompletionRequest(id)); }
    private UserProfile profile() { return profiles.findByUserId(userId).orElseThrow(); }
    private DailyProgress snapshot(LocalDate date) { return daily.findByUserIdAndProgressDate(userId, date).orElseThrow(); }
    private void qualify() { complete(task(true)); complete(task(false)); complete(task(false)); }

    @Test void normalQualificationRemainsProvisionalUntilDayEnds() {
        qualify();
        assertThat(progress.getTodayProgress(userId).isDayQualified()).isTrue();
        assertThat(profile().getCurrentStreak()).isZero();
        assertThat(profile().getBestStreak()).isZero();
        assertThat(profile().getLastEvaluatedDate()).isEqualTo(DAY.minusDays(1));
        advance(1); streaks.closeElapsedDays(userId);
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
        assertThat(profile().getBestStreak()).isEqualTo(1);
        assertThat(profile().getLastEvaluatedDate()).isEqualTo(DAY);
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void newMandatoryTaskImmediatelyInvalidatesQualificationAndCanBeCompleted(boolean template) {
        qualify();
        long late = template ? fromTemplate() : task(true);
        DailyProgressResponse p = progress.getTodayProgress(userId);
        assertThat(p.getCompletedValidTaskCount()).isEqualTo(3);
        assertThat(p.getRequiredTaskCount()).isEqualTo(3);
        assertThat(p.getMandatoryTasksRequired()).isEqualTo(2);
        assertThat(p.getMandatoryTasksCompleted()).isEqualTo(1);
        assertThat(p.isDayQualified()).isFalse();
        complete(task(false));
        assertThat(snapshot(DAY).isDayQualified()).isFalse();
        assertThat(profile().getCurrentStreak()).isZero();
        complete(late);
        assertThat(snapshot(DAY).isDayQualified()).isTrue();
        assertThat(profile().getCurrentStreak()).isZero();
        advance(1); streaks.closeElapsedDays(userId);
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
    }

    @Test void pendingMandatoryTaskAtClosureBreaksStreakAndKeepsBest() {
        qualify(); advance(1); streaks.closeElapsedDays(userId);
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
        // Existing mandatory task is not completed on the second day.
        complete(task(false)); complete(task(false)); complete(task(false));
        assertThat(snapshot(DAY.plusDays(1)).isDayQualified()).isFalse();
        advance(2); streaks.closeElapsedDays(userId);
        assertThat(profile().getCurrentStreak()).isZero(); assertThat(profile().getBestStreak()).isEqualTo(1);
        assertThat(profile().getLastEvaluatedDate()).isEqualTo(DAY.plusDays(1));
    }

    @Test void catchesUpMultipleDaysInOrderIncludingInactivityAndRetries() {
        long a = task(true), b = task(false), c = task(false);
        complete(a); complete(b); complete(c);
        advance(1); complete(a); complete(b); complete(c);
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
        advance(2); streaks.closeElapsedDays(userId);
        assertThat(profile().getCurrentStreak()).isEqualTo(2);
        streaks.finalizeDay(userId, DAY); streaks.finalizeDay(userId, DAY.plusDays(1));
        assertThat(profile().getCurrentStreak()).isEqualTo(2);
        advance(4); streaks.closeElapsedDays(userId);
        assertThat(profile().getCurrentStreak()).isZero(); assertThat(profile().getBestStreak()).isEqualTo(2);
        assertThat(snapshot(DAY.plusDays(3)).isDayQualified()).isFalse();
    }

    @Test void historicalCountersAndEvaluationTimeSurviveTaskAndTemplateChanges() {
        long late = fromTemplate(); complete(late); complete(task(false)); complete(task(false));
        advance(1); streaks.closeElapsedDays(userId);
        DailyProgress before = snapshot(DAY);
        tx.executeWithoutResult(s -> {
            var task = taskRepository.findById(late).orElseThrow();
            var template = task.getTemplate();
            task.setMandatory(false); task.setActive(false); taskRepository.save(task);
            template.setMandatory(false); template.setActive(false); templateRepository.save(template);
        });
        task(true);
        DailyProgressResponse historical = progress.recalculateDailyProgress(userId, DAY);
        assertThat(historical.isDayQualified()).isTrue();
        assertThat(historical.getMandatoryTasksRequired()).isEqualTo(before.getMandatoryTasksRequired());
        assertThat(historical.getMandatoryTasksCompleted()).isEqualTo(before.getMandatoryTasksCompleted());
        assertThat(historical.getEvaluatedAt()).isEqualTo(before.getEvaluatedAt());
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
    }

    @Test void rejectsTodayAndFutureClosureWithoutChangingState() {
        qualify();
        assertThatThrownBy(() -> streaks.finalizeDay(userId, DAY)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> streaks.finalizeDay(userId, DAY.plusDays(1))).isInstanceOf(BusinessException.class);
        assertThat(profile().getCurrentStreak()).isZero();
    }

    @Test void httpContractKeepsShapesAndFinalizesYesterdayByDefault() throws Exception {
        qualify();
        var principal = com.davidrr.grindprotocol.utils.TestAuthenticatedUserFactory.withId(userId);
        mvc.perform(get("/me/daily-progress/today").with(user(principal)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.dayQualified").value(true))
                .andExpect(jsonPath("$.requiredTaskCount").value(3))
                .andExpect(jsonPath("$.completedValidTaskCount").value(3));
        mvc.perform(post("/me/progression/finalize-day").with(user(principal)).with(csrf()).param("date", DAY.toString()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("DAY_NOT_CLOSED"));
        advance(1);
        for (int retry = 0; retry < 2; retry++) {
            mvc.perform(post("/me/progression/finalize-day").with(user(principal)).with(csrf()))
                    .andExpect(status().isNoContent());
        }
        mvc.perform(get("/me/profile").with(user(principal)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.currentStreak").value(1))
                .andExpect(jsonPath("$.bestStreak").value(1))
                .andExpect(jsonPath("$.lastEvaluatedDate").value(DAY.toString()));
    }

    @Test void repairsLegacyEarlyStreakBeforeIntroducingNewObligations() {
        qualify();
        tx.executeWithoutResult(s -> {
            DailyProgress previous = snapshot(DAY.minusDays(1));
            previous.setCompletedValidTaskCount(3);
            previous.setMandatoryTasksRequired(1);
            previous.setMandatoryTasksCompleted(1);
            previous.setDayQualified(true);
            previous.setEvaluatedAt(DAY.minusDays(1).atTime(23, 0));
            daily.save(previous);
            UserProfile p = profiles.findByUserId(userId).orElseThrow();
            p.setLastEvaluatedDate(DAY); p.setCurrentStreak(2); p.setBestStreak(2); profiles.save(p);
        });
        fromTemplate();
        assertThat(snapshot(DAY).isDayQualified()).isFalse();
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
        assertThat(profile().getBestStreak()).isEqualTo(1);
        assertThat(profile().getLastEvaluatedDate()).isEqualTo(DAY.minusDays(1));
    }

    @Test void simultaneousClosuresIncrementOnlyOnce() throws Exception {
        qualify(); advance(1);
        concurrent(() -> streaks.closeElapsedDays(userId), () -> streaks.closeElapsedDays(userId));
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
        assertThat(profile().getBestStreak()).isEqualTo(1);
    }

    @Test void mandatoryCreationAndOptionalCompletionSerializeWithoutStaleSnapshotOrLostRewards() throws Exception {
        qualify(); long optional = task(false);
        concurrent(() -> fromTemplate(), () -> complete(optional));
        assertThat(snapshot(DAY).getCompletedValidTaskCount()).isEqualTo(4);
        assertThat(snapshot(DAY).getMandatoryTasksRequired()).isEqualTo(2);
        assertThat(snapshot(DAY).isDayQualified()).isFalse();
        assertThat(profile().getTotalXp()).isEqualTo(400);
        assertThat(profile().getCorePoints()).isEqualTo(40);
    }

    @Test void closureAndCreationCannotRewriteYesterday() throws Exception {
        qualify(); advance(1);
        concurrent(() -> streaks.closeElapsedDays(userId), () -> fromTemplate());
        assertThat(snapshot(DAY).isDayQualified()).isTrue();
        assertThat(snapshot(DAY).getMandatoryTasksRequired()).isEqualTo(1);
        assertThat(snapshot(DAY.plusDays(1)).getMandatoryTasksRequired()).isEqualTo(2);
        assertThat(profile().getCurrentStreak()).isEqualTo(1);
    }

    @Test void profileLockIsTakenBeforeDateSamplingAndDailyWrites() throws Exception {
        qualify();
        CountDownLatch locked = new CountDownLatch(1), release = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> holder = pool.submit(() -> tx.executeWithoutResult(s -> {
                profiles.findLockedByUserId(userId).orElseThrow(); locked.countDown();
                try { if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("timeout"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
            }));
            assertThat(locked.await(5, TimeUnit.SECONDS)).isTrue();
            Future<?> writer = pool.submit(() -> task(true));
            assertThatThrownBy(() -> writer.get(250, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            advance(1); release.countDown();
            holder.get(10, TimeUnit.SECONDS); writer.get(10, TimeUnit.SECONDS);
            assertThat(snapshot(DAY).isDayQualified()).isTrue();
            assertThat(snapshot(DAY.plusDays(1)).getMandatoryTasksRequired()).isEqualTo(2);
            assertThat(profile().getCurrentStreak()).isEqualTo(1);
        } finally { release.countDown(); pool.shutdownNow(); }
    }

    private void concurrent(Runnable first, Runnable second) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);
        try {
            List<Future<?>> results = new ArrayList<>();
            for (Runnable action : List.of(first, second)) results.add(pool.submit(() -> {
                try { barrier.await(5, TimeUnit.SECONDS); action.run(); }
                catch (Exception e) { throw new IllegalStateException(e); }
            }));
            for (Future<?> result : results) result.get(20, TimeUnit.SECONDS);
        } finally { pool.shutdownNow(); }
    }
}
