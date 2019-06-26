package io.yooksi.commons.logger;

import io.yooksi.commons.define.IBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.Serializable;

class InitializationPackage<T extends Appender> {

    final LoggerControl loggerControl;
    final LoggerConfig loggerConfig;

    final AppenderType<T> type;
    final String[] names;
    final Level level;

    final Layout<? extends Serializable> layout;
    final String logFilePath;

    static class Builder<T extends Appender> implements IBuilder<InitializationPackage> {

        private final LoggerControl control;
        private final AppenderType<T> type;
        private final String[] names;
        private final Level level;

        private Layout<? extends Serializable> layout;
        private String logFilePath;

        private Builder(LoggerControl control, AppenderType<T> type, Level level) {

            this.control = control;
            this.type = type;
            this.names = type.getNames();
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
        names = type.getNames();
    }

    public InitializationPackage<T> copyWithLayout(Layout<? extends Serializable> layout) {
        return create(loggerControl, type).forFileAppender(layout, logFilePath).build();
    }

    public static <T extends Appender> Builder<T> create(LoggerControl control, AppenderType<T> type) {
        return new Builder<>(control, type, control.getLevel(type.getLevelType()));
    }
}
