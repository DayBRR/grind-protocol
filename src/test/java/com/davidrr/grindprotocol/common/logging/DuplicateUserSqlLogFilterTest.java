package com.davidrr.grindprotocol.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.spi.FilterReply;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class DuplicateUserSqlLogFilterTest {
    private final DuplicateUserSqlLogFilter filter = new DuplicateUserSqlLogFilter();
    private final LoggerContext context = new LoggerContext();

    @AfterEach
    void stopContext() {
        context.stop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk_users_username", "uk_users_email"})
    void removesOnlyKnownDuplicateDetailsBeforeTheyReachAppenders(String constraint) {
        var logger = context.getLogger("org.hibernate.engine.jdbc.spi.SqlExceptionHelper");
        assertThat(filter.decide(null, logger, Level.ERROR,
                "ERROR: duplicate key value violates unique constraint \"{}\"\nDetail: private value",
                new Object[]{constraint}, null)).isEqualTo(FilterReply.DENY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SQL Error: 0, SQLState: 23505", "connection refused",
            "unique constraint \"uk_other_email\"", "unique constraint \"uk_users_email_extra\""})
    void preservesOtherSqlDiagnostics(String message) {
        assertThat(filter.decide(null, context.getLogger("org.hibernate.engine.jdbc.spi.SqlExceptionHelper"),
                Level.ERROR, message, null, null)).isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void leavesOtherLoggersAndNullMessagesAlone() {
        assertThat(filter.decide(null, context.getLogger("other"), Level.ERROR,
                "unique constraint \"uk_users_email\"", null, null)).isEqualTo(FilterReply.NEUTRAL);
        assertThat(filter.decide(null, context.getLogger("other"), Level.ERROR,
                null, null, null)).isEqualTo(FilterReply.NEUTRAL);
    }
}
