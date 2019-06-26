package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@MethodsNotNull
@SuppressWarnings("WeakerAccess")
public class AppenderData<T extends Appender> {

    private final LoggerConfig loggerConfig;
    private final T appender;
    private final AppenderType<T> type;
    private LifeCycle.State state;
    private Level level;

    @SuppressWarnings("unchecked")
    AppenderData(LoggerConfig loggerConfig, Appender appender, AppenderType<T> type, @Nullable Level level) {

        this.loggerConfig = loggerConfig;
        this.appender = (T) appender;
        this.type = type;
        this.state = LifeCycle.State.STARTED;
        this.level = level != null ? level : Level.ALL;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
    public void setState(LifeCycle.State state) {
        this.state = state;
    }

    public AppenderType<T> getType() {
        return type;
    }
    public LifeCycle.State getState() {
        return state;
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
    public Layout<? extends Serializable> getLayout() {
        return appender.getLayout();
    }
}
