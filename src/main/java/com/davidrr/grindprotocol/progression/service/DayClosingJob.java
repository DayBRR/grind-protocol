package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "grind.day-closing.enabled", havingValue = "true", matchIfMissing = true)
public class DayClosingJob {
    private final UserProfileRepository profiles;
    private final StreakService streaks;

    @Scheduled(fixedDelayString = "${grind.day-closing.delay-ms:60000}")
    public void closeElapsedDays() {
        int page = 0;
        List<Long> ids;
        do {
            ids = profiles.findUserIds(PageRequest.of(page++, 200));
            for (Long id : ids) {
                try {
                    // Separate transaction per user; failures are retried on the next run.
                    streaks.closeElapsedDays(id);
                } catch (RuntimeException failure) {
                    log.error("Could not close elapsed days for user {}", id, failure);
                }
            }
        } while (ids.size() == 200);
    }
}
