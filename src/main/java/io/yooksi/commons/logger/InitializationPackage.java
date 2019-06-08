package io.yooksi.commons.logger;

import io.yooksi.commons.define.IBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.Serializable;

/**
 * Extended appender data container that holds information
 * used when retrieving, initializing or creating new appenders.
 *
 * @param <T> Appender implementation type
 * @see Log4jUtils#getOrSetupAppender(InitializationPackage)
 */
class InitializationPackage<T extends Appender> {

    final LoggerControl loggerControl;
    final LoggerConfig loggerConfig;

    final AppenderType<T> type;
    final Level level;

    final Layout<? extends Serializable> layout;
    final String logFilePath;

    static class Builder<T extends Appender> implements IBuilder<InitializationPackage> {

        private final LoggerControl control;
        private final AppenderType<T> type;
        private final Level level;

        private Layout<? extends Serializable> layout;
        private String logFilePath;

        private Builder(LoggerControl control, AppenderType<T> type, Level level) {

            this.control = control;
            this.type = type;
            this.level = level;
        }

        Builder<T> forFileAppender(Layout<? extends Serializable> layout, String logFilePath) {
            this.logFilePath = logFilePath; this.layout = layout; return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public InitializationPackage<T> build() {
            return new InitializationPackage(control, type, level, layout, logFilePath);
        }
    }

    private InitializationPackage(LoggerControl control, AppenderType<T> type, Level level,
                                  Layout<? extends Serializable> layout, String logFilePath) {

        this.type = type;
        this.level = level;
        this.layout = layout;
        this.logFilePath = logFilePath;

        loggerControl = control;
        loggerConfig = control.getLoggerConfig();
    }

    /**
     * Check if this initialization package represents a file appender that wants to
     * print to a dedicated log file relative to the given {@code AppenderData}.
     *
     * @return {@code true} if this initialization package represents a file appender type that
     *         has a <i>(non-empty)</i> log file path that is <b>different</b> then that resolved
     *         from the given {@code AppenderData}.
     */
    public boolean isDedicatedFileAppender(AppenderData<T> data) {

        return type == AppenderType.FILE && !logFilePath.isEmpty() && !logFilePath.equals(
                Log4jUtils.getLogFilePath(AppenderType.FILE.getTypeClass().cast(data.getAppender())));
    }

    /**
     * Creates and returns a new initialization package copy using the given layout.
     * The new package will have all of it's properties copied over except the layout.
     */
    public InitializationPackage<T> copyWithLayout(Layout<? extends Serializable> layout) {
        return create(loggerControl, type).forFileAppender(layout, logFilePath).build();
    }

    /**
     * Create and return a new package builder for the given {@code AppenderType}.
     * @param control {@code LoggerControl} used to manage this appender type
     */
    public static <T extends Appender> Builder<T> create(LoggerControl control, AppenderType<T> type) {
        return new Builder<>(control, type, control.getLevel(type.getLevelType()));
    }
}
