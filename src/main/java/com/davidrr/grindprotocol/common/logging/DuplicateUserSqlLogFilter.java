package com.davidrr.grindprotocol.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/** The domain handler logs these conflicts safely; PostgreSQL's detail contains account data. */
public class DuplicateUserSqlLogFilter extends TurboFilter {
    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable throwable) {
        if (logger == null || !"org.hibernate.engine.jdbc.spi.SqlExceptionHelper".equals(logger.getName())
                || format == null) {
            return FilterReply.NEUTRAL;
        }
        String message = MessageFormatter.arrayFormat(format, params).getMessage();
        if (message.contains("unique constraint \"uk_users_username\"")
                || message.contains("unique constraint \"uk_users_email\"")) {
            return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
    }
}
