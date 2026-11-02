package com.davidrr.grindprotocol.progression.time;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** One business instant per transaction, sampled after acquiring the profile lock. */
@Component
public class DayCalendar {
    private final Clock clock;
    private final Object transactionKey = new Object();

    public DayCalendar(Clock clock) {
        this.clock = clock;
    }

    public LocalDate today() {
        return now().toLocalDate();
    }

    public LocalDateTime now() {
        return LocalDateTime.ofInstant(instant(), clock.getZone());
    }

    private Instant instant() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return clock.instant();
        }
        Instant instant = (Instant) TransactionSynchronizationManager.getResource(transactionKey);
        if (instant == null) {
            instant = clock.instant();
            TransactionSynchronizationManager.bindResource(transactionKey, instant);
            Instant sampledInstant = instant;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void suspend() {
                    TransactionSynchronizationManager.unbindResource(transactionKey);
                }

                @Override
                public void resume() {
                    // This synchronization belongs to the suspended transaction.
                    TransactionSynchronizationManager.bindResource(transactionKey, sampledInstant);
                }

                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(transactionKey);
                }
            });
        }
        return instant;
    }
}
