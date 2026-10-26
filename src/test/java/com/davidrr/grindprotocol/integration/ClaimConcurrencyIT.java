package com.davidrr.grindprotocol.integration;

import com.davidrr.grindprotocol.achievement.enums.AchievementType;
import com.davidrr.grindprotocol.achievement.model.Achievement;
import com.davidrr.grindprotocol.achievement.model.UserAchievement;
import com.davidrr.grindprotocol.achievement.repository.AchievementRepository;
import com.davidrr.grindprotocol.achievement.repository.UserAchievementRepository;
import com.davidrr.grindprotocol.achievement.service.AchievementService;
import com.davidrr.grindprotocol.common.exception.BusinessException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.quest.enums.QuestFrequency;
import com.davidrr.grindprotocol.quest.enums.QuestStatus;
import com.davidrr.grindprotocol.quest.enums.QuestType;
import com.davidrr.grindprotocol.quest.model.Quest;
import com.davidrr.grindprotocol.quest.model.UserQuest;
import com.davidrr.grindprotocol.quest.repository.QuestRepository;
import com.davidrr.grindprotocol.quest.repository.UserQuestRepository;
import com.davidrr.grindprotocol.quest.service.QuestService;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

class ClaimConcurrencyIT extends AbstractPostgresIT {
    @Autowired AchievementService achievements;
    @Autowired QuestService quests;
    @Autowired AchievementRepository achievementRepository;
    @Autowired UserAchievementRepository userAchievementRepository;
    @Autowired QuestRepository questRepository;
    @Autowired UserQuestRepository userQuestRepository;
    @Autowired UserRepository users;
    @Autowired UserProfileRepository profiles;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbc;

    private TransactionTemplate tx;
    private Long userId;
    private Long profileId;
    private final List<Long> achievementIds = new ArrayList<>();
    private final List<Long> questIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            String name = "claim_" + UUID.randomUUID();
            User user = users.save(User.builder().username(name).email(name + "@test.com")
                    .password("unused").role("USER").enabled(true).build());
            userId = user.getId();
            profileId = profiles.save(UserProfile.builder().user(user).displayName(name).dailyTaskGoal(3)
                    .totalXp(90L).corePoints(10L).level(1).currentStreak(0).bestStreak(0).build()).getId();
        });
    }

    @AfterEach
    void cleanUpFixtures() {
        tx.executeWithoutResult(s -> {
            jdbc.update("delete from user_activity_events where fk_user_profile=?", profileId);
            jdbc.update("delete from user_achievements where fk_user_profile=?", profileId);
            jdbc.update("delete from user_quests where fk_user_profile=?", profileId);
            jdbc.update("delete from user_profiles where id=?", profileId);
            jdbc.update("delete from users where id=?", userId);
            achievementIds.forEach(id -> jdbc.update("delete from achievements where id=?", id));
            questIds.forEach(id -> jdbc.update("delete from quests where id=?", id));
        });
    }

    @Test
    void achievementSequential() {
        Long id = achievement();
        achievements.claimAchievement(id, userId);
        assertThat(claim(() -> achievements.claimAchievement(id, userId)))
                .isEqualTo(ErrorCodes.Achievement.ALREADY_CLAIMED);
        assertAchievement(id);
        assertBalances(115, 15, 1);
    }

    @Test
    void questSequential() {
        Long id = quest();
        quests.claimQuest(id, userId);
        assertThat(claim(() -> quests.claimQuest(id, userId))).isEqualTo(ErrorCodes.Quest.ALREADY_CLAIMED);
        assertQuest(id);
        assertBalances(140, 17, 1);
    }

    @Test
    void concurrentSameAchievement() throws Exception {
        Long id = achievement();
        concurrent(() -> achievements.claimAchievement(id, userId),
                () -> achievements.claimAchievement(id, userId), ErrorCodes.Achievement.ALREADY_CLAIMED);
        assertAchievement(id);
        assertBalances(115, 15, 1);
    }

    @Test
    void concurrentSameQuest() throws Exception {
        Long id = quest();
        concurrent(() -> quests.claimQuest(id, userId), () -> quests.claimQuest(id, userId),
                ErrorCodes.Quest.ALREADY_CLAIMED);
        assertQuest(id);
        assertBalances(140, 17, 1);
    }

    @Test
    void concurrentDifferentAchievements() throws Exception {
        Long first = achievement();
        Long second = achievement();
        concurrent(() -> achievements.claimAchievement(first, userId),
                () -> achievements.claimAchievement(second, userId), "OK");
        assertAchievement(first);
        assertAchievement(second);
        assertBalances(140, 20, 2);
    }

    @Test
    void concurrentDifferentQuests() throws Exception {
        Long first = quest();
        Long second = quest();
        concurrent(() -> quests.claimQuest(first, userId), () -> quests.claimQuest(second, userId), "OK");
        assertQuest(first);
        assertQuest(second);
        assertBalances(190, 24, 2);
    }

    @Test
    void concurrentAchievementAndQuest() throws Exception {
        Long a = achievement();
        Long q = quest();
        concurrent(() -> achievements.claimAchievement(a, userId), () -> quests.claimQuest(q, userId), "OK");
        assertAchievement(a);
        assertQuest(q);
        assertBalances(165, 22, 2);
    }

    @Test
    void rollbackRevertsAchievementRewardsStateAndEvent() {
        Long id = achievement();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> {
            achievements.claimAchievement(id, userId);
            throw new IllegalStateException("Force rollback after recording the event");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("select claimed from user_achievements where fk_user_profile=? and fk_achievement=?",
                Boolean.class, profileId, id)).isFalse();
        assertBalances(90, 10, 0);
    }

    @Test
    void rollbackRevertsQuestRewardsStateAndEvent() {
        Long id = quest();
        assertThatThrownBy(() -> tx.executeWithoutResult(s -> {
            quests.claimQuest(id, userId);
            throw new IllegalStateException("Force rollback after recording the event");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("select status from user_quests where fk_user_profile=? and fk_quest=?",
                String.class, profileId, id)).isEqualTo("COMPLETED");
        assertBalances(90, 10, 0);
    }

    private Long achievement() {
        return tx.execute(s -> {
            Achievement a = new Achievement();
            a.setCode("A_" + UUID.randomUUID()); a.setName("Achievement"); a.setDescription("test");
            a.setType(AchievementType.TOTAL_XP); a.setTargetValue(1L);
            a.setXpReward(25L); a.setCorePointsReward(5L); a.setEnabled(true); a.setHidden(false);
            achievementRepository.save(a);
            achievementIds.add(a.getId());
            UserAchievement ua = new UserAchievement();
            ua.setAchievement(a); ua.setUserProfile(profiles.findById(profileId).orElseThrow());
            ua.setUnlocked(true); ua.setUnlockedAt(LocalDateTime.now()); ua.setProgressValue(90L);
            userAchievementRepository.save(ua);
            return a.getId();
        });
    }

    private Long quest() {
        return tx.execute(s -> {
            Quest q = new Quest();
            q.setCode("Q_" + UUID.randomUUID()); q.setName("Quest"); q.setDescription("test");
            q.setType(QuestType.TASK_COMPLETION_COUNT); q.setFrequency(QuestFrequency.DAILY);
            q.setTargetValue(1L); q.setXpReward(50L); q.setCorePointsReward(7L); q.setEnabled(true);
            questRepository.save(q);
            questIds.add(q.getId());
            UserQuest uq = new UserQuest();
            uq.setQuest(q); uq.setUserProfile(profiles.findById(profileId).orElseThrow());
            uq.setStatus(QuestStatus.COMPLETED); uq.setCompletedAt(LocalDateTime.now());
            uq.setPeriodStart(LocalDate.now()); uq.setPeriodEnd(LocalDate.now()); uq.setProgressValue(1L);
            userQuestRepository.save(uq);
            return q.getId();
        });
    }

    private String claim(Runnable action) {
        try {
            action.run();
            return "OK";
        } catch (BusinessException e) {
            return e.getCode();
        }
    }

    private void concurrent(Runnable first, Runnable second, String secondResult) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstClaimed = new CountDownLatch(1);
        CountDownLatch commitFirst = new CountDownLatch(1);
        CompletableFuture<Integer> firstPid = new CompletableFuture<>();
        CompletableFuture<Integer> secondPid = new CompletableFuture<>();
        try {
            Future<String> a = executor.submit(() -> tx.execute(s -> {
                jdbc.execute("set local lock_timeout = '15s'");
                firstPid.complete(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
                String result = claim(first);
                firstClaimed.countDown();
                await(commitFirst);
                return result;
            }));
            assertThat(firstClaimed.await(15, TimeUnit.SECONDS)).isTrue();
            Future<String> b = executor.submit(() -> claim(() -> tx.executeWithoutResult(s -> {
                jdbc.execute("set local lock_timeout = '15s'");
                secondPid.complete(jdbc.queryForObject("select pg_backend_pid()", Integer.class));
                second.run();
            })));
            int pid = secondPid.get(15, TimeUnit.SECONDS);
            int blocker = firstPid.get(15, TimeUnit.SECONDS);
            assertThat(pid).isNotEqualTo(blocker);
            // Observe a real wait on the first transaction, not a scheduler-dependent race.
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            boolean blocked = false;
            while (System.nanoTime() < deadline && !b.isDone()) {
                blocked = Boolean.TRUE.equals(jdbc.queryForObject(
                        "select ? = any(pg_blocking_pids(?))", Boolean.class, blocker, pid));
                if (blocked) break;
            }
            assertThat(blocked).as("second transaction must wait for the first transaction's lock").isTrue();
            commitFirst.countDown();
            assertThat(a.get(15, TimeUnit.SECONDS)).isEqualTo("OK");
            assertThat(b.get(15, TimeUnit.SECONDS)).isEqualTo(secondResult);
        } finally {
            commitFirst.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(20, TimeUnit.SECONDS)).isTrue();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(15, TimeUnit.SECONDS)) throw new AssertionError("Timed out waiting for commit");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
    }

    private void assertAchievement(Long id) {
        tx.executeWithoutResult(s -> {
            UserAchievement ua = userAchievementRepository.findByUserProfileUserIdAndAchievementId(userId, id).orElseThrow();
            assertThat(ua.getClaimed()).isTrue(); assertThat(ua.getClaimedAt()).isNotNull();
        });
        assertEvent("ACHIEVEMENT_CLAIMED", id, 25, 5);
    }

    private void assertQuest(Long id) {
        tx.executeWithoutResult(s -> {
            UserQuest uq = userQuestRepository.findByUserProfileUserIdAndQuestIdAndPeriodStartAndPeriodEnd(
                    userId, id, LocalDate.now(), LocalDate.now()).orElseThrow();
            assertThat(uq.getStatus()).isEqualTo(QuestStatus.CLAIMED); assertThat(uq.getClaimedAt()).isNotNull();
        });
        assertEvent("QUEST_CLAIMED", id, 50, 7);
    }

    private void assertEvent(String type, Long source, int xp, int cp) {
        assertThat(jdbc.queryForObject("select count(*) from user_activity_events where fk_user_profile=? and type=? and source_id=? and xp_delta=? and core_points_delta=?",
                Long.class, profileId, type, source, xp, cp)).isEqualTo(1);
    }

    private void assertBalances(long xp, long cp, long events) {
        UserProfile profile = profiles.findById(profileId).orElseThrow();
        assertThat(profile.getTotalXp()).isEqualTo(xp); assertThat(profile.getCorePoints()).isEqualTo(cp);
        assertThat(jdbc.queryForObject("select count(*) from user_activity_events where fk_user_profile=?",
                Long.class, profileId)).isEqualTo(events);
    }
}
