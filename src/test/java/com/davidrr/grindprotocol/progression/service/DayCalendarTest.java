package com.davidrr.grindprotocol.progression.service;

import com.davidrr.grindprotocol.progression.time.DayCalendar;
import com.davidrr.grindprotocol.progression.time.DayConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DayCalendarTest {
    @Test void usesBusinessZoneRatherThanUtcAtMidnight() {
        DayCalendar calendar = new DayCalendar(Clock.fixed(
                Instant.parse("2026-11-02T23:30:00Z"), ZoneId.of("Europe/Madrid")));
        assertThat(calendar.today()).isEqualTo(LocalDate.of(2026, 11, 3));
    }

    @Test void keepsOneInstantThroughoutTransactionAcrossMidnightAndCleansUp() {
        Clock clock = mock(Clock.class);
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/Madrid"));
        when(clock.instant()).thenReturn(Instant.parse("2026-11-02T22:59:59Z"), Instant.parse("2026-11-02T23:00:01Z"));
        DayCalendar calendar = new DayCalendar(clock);
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThat(calendar.today()).isEqualTo(LocalDate.of(2026, 11, 2));
            assertThat(calendar.now()).isEqualTo(LocalDateTime.of(2026, 11, 2, 23, 59, 59));
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
        assertThat(calendar.today()).isEqualTo(LocalDate.of(2026, 11, 3));
    }

    @Test void daylightSavingUsesCalendarDaysOf23And25Hours() {
        ZoneId zone = ZoneId.of("Europe/Madrid");
        LocalDate spring = LocalDate.of(2026, 3, 29), autumn = LocalDate.of(2026, 10, 25);
        for (LocalDate day : new LocalDate[]{spring, autumn}) {
            Instant start = day.atStartOfDay(zone).toInstant();
            Instant end = day.plusDays(1).atStartOfDay(zone).toInstant();
            assertThat(Duration.between(start, end).toHours()).isEqualTo(day.equals(spring) ? 23 : 25);
            assertThat(new DayCalendar(Clock.fixed(end.minusNanos(1), zone)).today()).isEqualTo(day);
            assertThat(new DayCalendar(Clock.fixed(end, zone)).today()).isEqualTo(day.plusDays(1));
        }
    }

    @Test void validatesConfiguredZoneAndPreservesJvmDefaultWhenUnset() {
        DayConfiguration config = new DayConfiguration();
        assertThat(config.businessClock("").getZone()).isEqualTo(ZoneId.systemDefault());
        assertThat(config.businessClock("Europe/Madrid").getZone()).isEqualTo(ZoneId.of("Europe/Madrid"));
        assertThatThrownBy(() -> config.businessClock("not-a-zone")).isInstanceOf(DateTimeException.class);
    }
}
