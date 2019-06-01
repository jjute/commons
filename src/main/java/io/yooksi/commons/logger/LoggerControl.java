package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

import static io.yooksi.commons.logger.LoggerLevels.Type;

@MethodsNotNull
@SuppressWarnings("WeakerAccess")
public class LoggerControl {
    /*
     * Like most collection implementations EnumMap is not synchronized. If multiple threads access
     * an enum map concurrently, and at least one of the threads modifies the map, it should be
     * synchronized externally. Wrap the map like so to prevent accidental unsynchronized access.
     */
    private final java.util.Map<AppenderType, AppenderData> appenderDataMap =
            java.util.Collections.synchronizedMap(new java.util.HashMap<>());

    private final String name;
    private final Logger logger;
    private final LoggerLevels levels;
    private final LoggerContext context;
    private final Configuration config;
    private final LoggerConfig loggerConfig;

    private LoggerControl(String name, LoggerLevels levels, boolean currentContext, boolean additive) {

        this.levels = levels;
        this.name = name;
        /*
         * Try to find a wider context here with currentContext parameter passed as false.
         * If there already is larger LoggerContext it will be used instead of our local one.
         */
        context = LoggerContext.getContext(currentContext);
        config = context.getConfiguration();
        loggerConfig = Log4jUtils.getOrCreateLoggerConfig(name, config, getLevel(Type.LOGGER), additive);
        logger = context.getLogger(name);

        loggerConfig.setAdditive(additive);
        loggerConfig.setLevel(getLevel(Type.LOGGER));

        String log = "Constructed %s LoggerControl using %s Configuration found in %s context %s with levels %s";
        CommonLogger.LOGGER.printf(Level.DEBUG, log, additive ? "additive" : "non-additive", config.getName(),
                currentContext ? "current" : "external", context.getName(), levels.toString());
    }

    LoggerControl(String name, Level level, LoggerContext context) {

        this.name = name;
        this.context = context;

        levels = new LoggerLevels(level);
        logger = context.getLogger(name);

        config = context.getConfiguration();
        loggerConfig = config.getLoggerConfig(name);
    }

    public static LoggerControl create(String name, Level consoleLevel, Level fileLevel, boolean currentContext, boolean additive) {
        return new LoggerControl(name, new LoggerLevels(Level.ALL, consoleLevel, fileLevel), currentContext, additive);
    }
    public LoggerControl withAppenders(String logFilePath) {

        AppenderData<Appender> console = Log4jUtils.getOrSetupAppender(AppenderType.CONSOLE.getBuilder(this).build());
        registerAppender(AppenderType.CONSOLE, console);

        InitializationPackage.Builder<AbstractOutputStreamAppender> builder =
                AppenderType.FILE.getBuilder(this).forFileAppender(console.getLayout(), logFilePath);

        AppenderData<AbstractOutputStreamAppender> file = Log4jUtils.getOrSetupAppender(builder.build());
        registerAppender(AppenderType.FILE, file);

        return this;
    }

    private synchronized <T extends Appender> void registerAppender(AppenderType<T> type, AppenderData<T> data) {

        if (data.isLoggerConfig(loggerConfig)) {
            if (appenderDataMap.containsKey(type)) {
                CommonLogger.LOGGER.warn("Overriding already registered %s", type.toString());
            }
            appenderDataMap.put(type, data);
        }
    }

    /**
     * Update logger appender by removing the existing appender entry from
     * {@code LoggerConfig} and adding it again with a different level.
     *
     * @param type used to find the appender to update from internal map
     * @param level new {@code log4j} level we want to update the appender to.
     * Note that the level needs to be different then the level currently held
     * by the target appender or the appender will not be updated.
     */
    public <T extends Appender> void updateAppender(AppenderType<T> type, Level level, @Nullable Filter filter) {

        AppenderData<T> data = getAppenderData(type);
        if (levels.getLevel(type).intLevel() != level.intLevel())
        {
            if (data != null) updateAppender(data, type, level, filter);
            else CommonLogger.LOGGER.warn("Tried to update non-existing %s %s in LoggerConfig %s!",
                    type.toString(), type.getClass().getSimpleName(), loggerConfig.getName());
        }
        else {
            String log = "Tried to update %s %s in LoggerConfig %s to level %s, but appender is already at that level";
            CommonLogger.LOGGER.warn(log, type.toString(), type.getClass().getSimpleName(), loggerConfig.getName(), level);
        }
    }

    public static <T extends Appender> void updateAppender(AppenderData<T> data, InitializationPackage<T> ipackage, @Nullable Filter filter) {
        ipackage.loggerControl.updateAppender(data, ipackage.type, ipackage.level, filter);
    }

    private <T extends Appender> void updateAppender(AppenderData<T> data, AppenderType<T> type, Level level, @Nullable Filter filter) {

        CommonLogger.LOGGER.debug("Updating %s %s in LoggerConfig %s to level %s",
                type.toString(), type.getClass().getSimpleName(), data.getLoggerConfig().getName(), level);

        loggerConfig.removeAppender(data.getAppender().getName());
        loggerConfig.addAppender(data.getAppender(), level, filter);

        data.setLevel(level);
        levels.setLevel(type, level);
        context.updateLoggers();
    }

    public void startAppender(AppenderType type) {

        AppenderData data = appenderDataMap.get(type);
        if (data != null)
        {
            Appender appender = data.getAppender();
            if (!loggerConfig.getAppenders().containsKey(appender.getName()))
            {
                loggerConfig.addAppender(appender, data.getLevel(), null);
                data.setState(LifeCycle.State.STARTED);
            }
            else {
                String log = "Unable to start %s, appender already exists in LoggerConfig %s";
                CommonLogger.LOGGER.warn(log, type.toString(), loggerConfig.getName());
            }
        }
        else {
            String log = "Unable to start %s in LoggerConfig %s, appender not found in DataMap.";
            CommonLogger.LOGGER.throwing(new NoSuchElementException(String.format(log, type.toString(), loggerConfig.getName())));
        }
    }

    public void stopAppender(AppenderType type) {

        AppenderData data = appenderDataMap.get(type);
        if (data != null)
        {
            loggerConfig.removeAppender(data.getAppender().getName());
            data.setState(LifeCycle.State.STOPPED);
        }
        else {
            String log = "Unable to stop %s in LoggerConfig %s, appender not found in DataMap.";
            CommonLogger.LOGGER.throwing(new NoSuchElementException(String.format(log, type.toString(), loggerConfig.getName())));
        }
    }

    /**
     * This causes all Loggers in the current context to re-fetch information from
     * their {@code LoggerConfig}. It's important to call this after every change to
     * {@code LoggerConfig} otherwise the changes will not take place.
     */
    public void update() {
        context.updateLoggers();
    }

    public String getName() {
        return name;
    }
    public Logger getLogger() {
        return logger;
    }
    public Level getLevel(LoggerLevels.Type type) {
        return levels.getLevel(type);
    }
    public LoggerContext getContext() {
        return context;
    }
    public Configuration getConfiguration() {
        return config;
    }
    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }

    public String getLogFilePath() {

        AppenderData<AbstractOutputStreamAppender> data = getAppenderData(AppenderType.FILE);
        return data != null ? Log4jUtils.getLogFilePath(data.getAppender()) : "";
    }
}
