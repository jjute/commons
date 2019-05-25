package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.Nullable;

@MethodsNotNull
@SuppressWarnings("WeakerAccess")
public class AppenderData<T extends Appender> {

    private final LoggerConfig loggerConfig;
    private final T appender;
    private final Level level;

    @SuppressWarnings("unchecked")
    AppenderData(LoggerConfig loggerConfig, Appender appender, @Nullable Level level) {

        this.loggerConfig = loggerConfig;
        this.appender = (T) appender;
        this.level = level != null ? level : Level.ALL;
    }

    public void setFilter(LoggerContext context, Filter filter) {
        Log4jUtils.updateAppender(loggerConfig, context, appender, level, filter);
    }

    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }
    public T getAppender() {
        return appender;
    }
    public Level getLevel() {
        return level;
    }
    public int IntLevel() {
        return level.intLevel();
    }
    public boolean isFiltering(Level level) {
        return level.intLevel() > IntLevel();
    }

    public boolean isLevel(Level level) {
        return level.equals(this.level);
    }
    public boolean isLoggerConfig(LoggerConfig config) {
        return config.equals(loggerConfig);
    }
}
