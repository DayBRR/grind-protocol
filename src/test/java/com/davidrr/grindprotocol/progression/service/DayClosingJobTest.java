package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.userprofile.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.stream.LongStream;
import static org.mockito.Mockito.*;

class DayClosingJobTest {
    @Test void pagesUsersAndContinuesAfterAFailedClose() {
        UserProfileRepository profiles = mock(UserProfileRepository.class);
        StreakService streaks = mock(StreakService.class);
        when(profiles.findUserIds(PageRequest.of(0, 200))).thenReturn(LongStream.rangeClosed(1, 200).boxed().toList());
        when(profiles.findUserIds(PageRequest.of(1, 200))).thenReturn(List.of(201L));
        doThrow(new IllegalStateException("temporary failure")).when(streaks).closeElapsedDays(1L);
        new DayClosingJob(profiles, streaks).closeElapsedDays();
        verify(streaks).closeElapsedDays(2L);
        verify(streaks).closeElapsedDays(201L);
    }
}
