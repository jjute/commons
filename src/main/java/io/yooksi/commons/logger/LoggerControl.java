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

    /**
     * System property Key found in VM arguments for execution config
     * that defines the console logging level for internal library loggers.
     */
    private static final String LEVEL_VM_ARG = "commonLoggerLevel";

    /**
     * <p>Stores {@code AppenderData} objects for each {@code AppenderType}.</p>
     * This is a registry of all appenders (along with extended information contained in
     * {@code AppenderData}) that belong to this control. Do <b>not</b> access the map directly
     * to either add, remove or read elements. Instead use one of the supplied methods below.
     *
     * @see #getAppenderData(AppenderType)
     * @see #registerAppender(AppenderType, AppenderData)
     */
    private final java.util.Map<AppenderType, AppenderData> appenderDataMap =
            java.util.Collections.synchronizedMap(new java.util.HashMap<>());

    private final String name;
    private final Logger logger;
    private final LoggerLevels levels;
    private final LoggerContext context;
    private final Configuration config;
    private final LoggerConfig loggerConfig;

    /**
     * <p>This is an internal constructor, don't invoke from outside.</p>
     * Instead use one of {@code create} methods listed below.
     *
     * @param name name to associate the {@code LoggerConfig} with. It will be used to
     *             try to find an existing configuration, before trying to create a new one.
     * @param levels used to set logging thresholds
     *
     * @see #create(String, Level, Level, boolean, boolean)
     */
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

    /**
     * <p>Create a new control for given context with console level set to {@link #LEVEL_VM_ARG}
     * <i>(or {@code INFO} if no system property with that name was found)</i> and file and
     * {@code LoggerConfig} set to {@code ALL}.</p>
     *
     * This {@code LoggerControl} is intended for library loggers only.
     *
     * @param name name to associate the {@code LoggerConfig} with. It will be used to
     *             try to find an existing configuration, before trying to create a new one.
     * @param context internal {@code LoggerContext} to use
     */
    LoggerControl(String name, LoggerContext context) {

        String sConsoleLevel = System.getProperty(LEVEL_VM_ARG);
        Level consoleLevel = Level.toLevel(sConsoleLevel, Level.INFO);

        this.name = name;
        this.context = context;

        levels = new LoggerLevels(Level.ALL, consoleLevel, Level.ALL);
        logger = context.getLogger(name);

        config = context.getConfiguration();
        loggerConfig = config.getLoggerConfig(name);
    }

    /**
     * Construct and return a new {@code LoggerControl} instance.
     *
     * @param name name to associate the {@code LoggerConfig} with.
     * @param consoleLevel threshold level for logging to console
     * @param fileLevel threshold level for logging to file
     * @param currentContext whether to use an internal or external context, with internal context being tied
     *                       to our local {@code log4j2} xml file and defined in {@link CommonLogger#CONTEXT}
     * @param additive whether log events should be propagated down the chain of {@code LoggerConfig} parents
     * @return newly constructed {@code LoggerControl} with provided parameters and
     *         {@code LoggerConfig} threshold level set to {@code ALL}.
     *
     * @see #LoggerControl(String, LoggerLevels, boolean, boolean)
     */
    public static LoggerControl create(String name, Level consoleLevel, Level fileLevel, boolean currentContext, boolean additive) {
        return new LoggerControl(name, new LoggerLevels(Level.ALL, consoleLevel, fileLevel), currentContext, additive);
    }

    /**
     * <p>Retrieve, initialize or construct available appenders and register them internally.</p>
     * Intended to be chain-called after one of the available {@code create} methods.
     *
     * @param logFilePath path to the dedicated log file we want to output logging to.
     *                    An empty string will disable logging to a dedicated file.
     * @return the same instance of {@code LoggerControl} used to invoke the method.
     * @see #create(String, Level, Level, boolean, boolean)
     */
    public LoggerControl withAppenders(String logFilePath) {

        AppenderData<Appender> console = Log4jUtils.getOrSetupAppender(AppenderType.CONSOLE.getBuilder(this).build());
        registerAppender(AppenderType.CONSOLE, console);

        InitializationPackage.Builder<AbstractOutputStreamAppender> builder =
                AppenderType.FILE.getBuilder(this).forFileAppender(console.getLayout(), logFilePath);

        AppenderData<AbstractOutputStreamAppender> file = Log4jUtils.getOrSetupAppender(builder.build());
        registerAppender(AppenderType.FILE, file);

        return this;
    }

    /**
     * Put the given {@code AppenderData} under given {@code AppenderType}
     * in the internal data map. Note that the appender data will only be
     * registered if it belongs to this control's {@code LoggerConfig}.
     *
     * @param type map key to associate with the data object
     * @param data map value containing extended appender info
     * @param <T> ensure that {@code data} is of appropriate type
     * @see #appenderDataMap
     */
    private synchronized <T extends Appender> void registerAppender(AppenderType<T> type, AppenderData<T> data) {
        /*
         * Do not register appenders that do not belong to this LoggerConfig.
         */
        if (data.isLoggerConfig(loggerConfig)) {
            if (appenderDataMap.containsKey(type)) {
                CommonLogger.LOGGER.warn("Overriding already registered %s", type.toString());
            }
            appenderDataMap.put(type, data);
        }
    }

    /**
     * Retrieve {@code AppenderData} associated with the given
     * {@code AppenderType} from the data registry
     *
     * @param <T> used to cast {@code AppenderData} to the appropriate type
     * @return map value associated with the given {@code AppenderType}.
     */
    @SuppressWarnings("unchecked")
    public synchronized @Nullable <T extends Appender> AppenderData<T> getAppenderData(AppenderType<T> type) {
        return appenderDataMap.get(type);
    }

    /**
     * <p>Update logger appender by removing the existing appender entry from
     * {@code LoggerConfig} and adding it again with a different level.</p>
     *
     * <i>Note that the level needs to be different then the level currently held
     * by the target appender or the appender will not be updated.</i>
     *
     * @param type used to find the {@code AppenderData} from data registry
     * @param level new level to update the appender to
     * @param filter a filter for the Appender reference
     * @param <T> used to cast the found registry value
     * @see #updateAppender(AppenderData, AppenderType, Level, Filter)
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

    /**
     * Helper method to update the appender supplied in {@code AppenderData}
     * to a new level supplied in {@code iPackage}. This method is intended to
     * be called from appender initialization methods.
     *
     * @param iPackage used to retrieve {@code AppenderType} and {@code Level}
     * @param filter a filter for the Appender reference
     * @param <T> used to ensure parameters are the same type
     * @see #updateAppender(AppenderData, AppenderType, Level, Filter)
     */
    public static <T extends Appender> void updateAppender(AppenderData<T> data, InitializationPackage<T> iPackage, @Nullable Filter filter) {
        iPackage.loggerControl.updateAppender(data, iPackage.type, iPackage.level, filter);
    }

    /**
     * Internal method to update logger appender by removing the existing appender
     * entry from {@code LoggerConfig} and adding it again with a different level.
     *
     * @param data contains the Appender object we want to update
     * @param type used to find the appender to update from internal map
     * @param level new {@code log4j} level we want to update the appender to
     * @param filter a filter for the Appender reference
     */
    private <T extends Appender> void updateAppender(AppenderData<T> data, AppenderType<T> type, Level level, @Nullable Filter filter) {

        if (!CommonLogger.isInitializing()) {
            CommonLogger.LOGGER.debug("Updating %s %s in LoggerConfig %s to level %s",
                    type.toString(), type.getClass().getSimpleName(), data.getLoggerConfig().getName(), level);
        }
        loggerConfig.removeAppender(data.getAppender().getName());
        loggerConfig.addAppender(data.getAppender(), level, filter);
        /*
         * Update internal data here
         */
        data.setLevel(level);
        levels.setLevel(type, level);
        /*
         * Update our LoggerConfig against current Configuration
         */
        context.updateLoggers();
    }

    /**
     * <p>Adds a registered appender of given type in the current {@code LoggerConfig}.</p>
     * This method is intended to be used after the appender has been programmatically
     * stopped with {@link #stopAppender(AppenderType)}.
     */
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

    /**
     * <p>Removes a registered appender of given type in the current {@code LoggerConfig}.</p>
     * <p>This will essentially stop any form of logging coming from that appender.</p>
     * If you wish to start the appender again call {@link #startAppender(AppenderType)}.
     */
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
