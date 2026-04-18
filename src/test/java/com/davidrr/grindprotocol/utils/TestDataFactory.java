package com.davidrr.grindprotocol.utils;

import com.davidrr.grindprotocol.task.dto.CreateTaskCompletionRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskFromTemplateRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskRequest;
import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import com.davidrr.grindprotocol.task.enums.CompletionSource;
import com.davidrr.grindprotocol.task.enums.TaskCategory;
import com.davidrr.grindprotocol.task.enums.TaskDifficulty;
import com.davidrr.grindprotocol.task.enums.TaskType;
import com.davidrr.grindprotocol.task.model.DailyProgress;
import com.davidrr.grindprotocol.task.model.Task;
import com.davidrr.grindprotocol.task.model.TaskCompletion;
import com.davidrr.grindprotocol.task.model.TaskTemplate;
import com.davidrr.grindprotocol.task.model.Trait;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.userprofile.dto.UpdateUserProfileRequest;
import com.davidrr.grindprotocol.userprofile.model.UserProfile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public final class TestDataFactory {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private TestDataFactory() {
    }

    public static User user() {
        return user(1L, "david", "david@test.com");
    }

    public static User user(Long id, String username, String email) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(PASSWORD_ENCODER.encode("Password123!"))
                .enabled(true)
                .build();
    }

    public static UserProfile userProfile(User user) {
        return UserProfile.builder()
                .id(1L)
                .user(user)
                .displayName(user.getUsername())
                .dailyTaskGoal(3)
                .totalXp(0L)
                .corePoints(0L)
                .currentStreak(0)
                .bestStreak(0)
                .lastEvaluatedDate(null)
                .build();
    }

    public static UpdateUserProfileRequest updateUserProfileRequest() {
        return UpdateUserProfileRequest.builder()
                .displayName("David Ruiz")
                .dailyTaskGoal(5)
                .build();
    }

    public static Trait trait() {
        return trait(1L, "DISCIPLINE", "Discipline");
    }

    public static Trait trait(Long id, String code, String name) {
        return Trait.builder()
                .id(id)
                .code(code)
                .name(name)
                .description(name + " description")
                .active(true)
                .build();
    }

    public static Set<Trait> traits() {
        Set<Trait> traits = new LinkedHashSet<>();
        traits.add(trait(1L, "DISCIPLINE", "Discipline"));
        traits.add(trait(2L, "FOCUS", "Focus"));
        return traits;
    }

    public static CreateTaskRequest createTaskRequest() {
        return CreateTaskRequest.builder()
                .title("Estudiar Spring")
                .description("Repasar seguridad y tests")
                .category(TaskCategory.WORK)
                .difficulty(TaskDifficulty.MEDIUM)
                .taskType(TaskType.DAILY)
                .baseXp(100)
                .mandatory(true)
                .streakEligible(true)
                .repeatable(false)
                .maxCompletionsPerDay(1)
                .diminishingReturnsEnabled(false)
                .active(true)
                .dueTime(null)
                .weeklyClosingDay(null)
                .traitCodes(Set.of("DISCIPLINE", "FOCUS"))
                .build();
    }

    public static Task task(User user) {
        return task(1L, user, true);
    }

    public static Task task(Long id, User user) {
        return task(id, user, true);
    }

    public static Task task(Long id, User user, boolean mandatory) {
        return Task.builder()
                .id(id)
                .user(user)
                .template(null)
                .title("Estudiar Spring")
                .description("Repasar seguridad y tests")
                .category(TaskCategory.WORK)
                .difficulty(TaskDifficulty.MEDIUM)
                .taskType(TaskType.DAILY)
                .baseXp(100)
                .mandatory(mandatory)
                .streakEligible(true)
                .repeatable(false)
                .maxCompletionsPerDay(1)
                .diminishingReturnsEnabled(false)
                .active(true)
                .dueTime(null)
                .weeklyClosingDay(null)
                .traits(traits())
                .build();
    }

    public static Task repeatableTask(User user) {
        return Task.builder()
                .id(2L)
                .user(user)
                .template(null)
                .title("Leer")
                .description("Leer 10 páginas")
                .category(TaskCategory.MIND)
                .difficulty(TaskDifficulty.EASY)
                .taskType(TaskType.DAILY)
                .baseXp(40)
                .mandatory(false)
                .streakEligible(true)
                .repeatable(true)
                .maxCompletionsPerDay(3)
                .diminishingReturnsEnabled(true)
                .active(true)
                .dueTime(null)
                .weeklyClosingDay(null)
                .traits(new LinkedHashSet<>())
                .build();
    }

    public static CreateTaskTemplateRequest createTaskTemplateRequest() {
        return CreateTaskTemplateRequest.builder()
                .title("Template Spring")
                .description("Template para estudiar")
                .category(TaskCategory.WORK)
                .difficulty(TaskDifficulty.HARD)
                .taskType(TaskType.DAILY)
                .baseXp(120)
                .mandatory(true)
                .streakEligible(true)
                .repeatable(false)
                .maxCompletionsPerDay(1)
                .diminishingReturnsEnabled(false)
                .dueTime(LocalTime.of(20, 0))
                .weeklyClosingDay(null)
                .publicTemplate(true)
                .active(true)
                .traitCodes(Set.of("DISCIPLINE", "FOCUS"))
                .build();
    }

    public static TaskTemplate taskTemplate(User creator) {
        return TaskTemplate.builder()
                .id(1L)
                .creatorUser(creator)
                .title("Template Spring")
                .description("Template para estudiar")
                .category(TaskCategory.WORK)
                .difficulty(TaskDifficulty.HARD)
                .taskType(TaskType.DAILY)
                .baseXp(120)
                .mandatory(true)
                .streakEligible(true)
                .repeatable(false)
                .maxCompletionsPerDay(1)
                .diminishingReturnsEnabled(false)
                .dueTime(LocalTime.of(20, 0))
                .weeklyClosingDay(null)
                .publicTemplate(true)
                .active(true)
                .traits(traits())
                .build();
    }

    public static CreateTaskFromTemplateRequest createTaskFromTemplateRequest() {
        return CreateTaskFromTemplateRequest.builder()
                .templateId(1L)
                .titleOverride("Task desde template")
                .descriptionOverride("Override description")
                .dueTimeOverride(LocalTime.of(21, 0))
                .weeklyClosingDayOverride(null)
                .build();
    }

    public static CreateTaskCompletionRequest createTaskCompletionRequest(Long taskId) {
        return CreateTaskCompletionRequest.builder()
                .taskId(taskId)
                .notes("Completada desde test")
                .build();
    }

    public static TaskCompletion taskCompletion(Task task, User user) {
        return taskCompletion(1L, task, user, 1, true, true);
    }

    public static TaskCompletion taskCompletion(Long id, Task task, User user) {
        return taskCompletion(id, task, user, 1, true, true);
    }

    public static TaskCompletion taskCompletion(Long id, Task task, User user,
                                                boolean countedForDailyGoal) {
        return taskCompletion(id, task, user, 1, countedForDailyGoal, true);
    }

    public static TaskCompletion taskCompletion(
            Long id,
            Task task,
            User user,
            Integer indexForDay,
            boolean countedForDailyGoal,
            boolean countedForStreak
    ) {
        return TaskCompletion.builder()
                .id(id)
                .task(task)
                .user(user)
                .completedAt(LocalDateTime.of(2026, 4, 18, 10, 0))
                .completionDate(LocalDate.of(2026, 4, 18))
                .completionIndexForDay(indexForDay)
                .countedForDailyGoal(countedForDailyGoal)
                .countedForStreak(countedForStreak)
                .baseXp(task.getBaseXp())
                .awardedXp(task.getBaseXp())
                .awardedCorePoints(task.getBaseXp() / 10)
                .notes("Completada desde test")
                .source(CompletionSource.MANUAL)
                .build();
    }

    public static DailyProgress dailyProgress(User user) {
        return DailyProgress.builder()
                .id(1L)
                .user(user)
                .progressDate(LocalDate.of(2026, 4, 18))
                .requiredTaskCount(3)
                .completedValidTaskCount(2)
                .mandatoryTasksRequired(1)
                .mandatoryTasksCompleted(1)
                .dayQualified(false)
                .evaluatedAt(LocalDateTime.of(2026, 4, 18, 22, 0))
                .build();
    }
}