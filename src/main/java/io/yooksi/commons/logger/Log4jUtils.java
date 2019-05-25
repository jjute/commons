package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import io.yooksi.commons.util.ReflectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.FileManager;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Log4jUtils {

    public static final Level[] VALID_LEVELS = { Level.FATAL, Level.ERROR,
            Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE, Level.ALL };

    /* Make the constructor private to disable instantiation */
    private Log4jUtils() {
        throw new UnsupportedOperationException();
    }

    public static String getStandardLogFilePath(String logger) {
        return "logs/" + logger + ".log";
    }

    /**
     * Create a new {@code ConsoleAppender} instance with default {@code PatternLayout}
     * for the supplied {@code Configuration}.
     */
    public static ConsoleAppender createNewConsoleAppender(CommonLogger logger, Layout<? extends java.io.Serializable> layout,
                                                           Configuration config, boolean initialize, @Nullable Filter filter) {

        CommonLogger.LOGGER.debug("Creating new ConsoleAppender for logger %s", logger.name);

        String name = CommonLogger.CONSOLE_APPENDERS[0];
        ConsoleAppender consoleAppender = ConsoleAppender.newBuilder().setName(name).setLayout(layout).setFilter(filter).build();

        return initialize ? initializeAppender(logger, consoleAppender, logger.logLevel) : consoleAppender;
    }

    public static ConsoleAppender createNewConsoleAppender(CommonLogger logger, AppenderData data, Configuration config, boolean initialize, @Nullable Filter filter) {
        return createNewConsoleAppender(logger, data.getAppender().getLayout(), config, initialize, filter);
    }

    public static ConsoleAppender createNewConsoleAppender(CommonLogger logger, Configuration config, boolean initialize) {

        PatternLayout layout = createPatternLayout(PatternLayout.SIMPLE_CONVERSION_PATTERN, config);
        return createNewConsoleAppender(logger, layout, config, initialize, null);
    }

    public static PatternLayout createPatternLayout(String pattern, Configuration config) {
        return PatternLayout.newBuilder().withPattern(pattern).withConfiguration(config).build();
    }

    /**
     * Create a new {@code FileAppender} instance with the supplied {@code Layout}.
     *
     * @param layout logging format the appender will use
     * @param logFilePath path to the logfile the appender will print to
     * @throws ExceptionInInitializerError when the {@code logFilePath} is already
     * associated with an existing {@code AbstractOutputStreamAppender} with a class
     * different then {@code FileAppender}.
     */
    public static FileAppender createNewFileAppender(CommonLogger logger, Layout<? extends Serializable> layout,
                                                     String logFilePath, boolean initialize) throws ExceptionInInitializerError {

        CommonLogger.LOGGER.debug( "Creating new FileAppender for logger %s", logger.name);

        FileAppender fileAppender = FileAppender.newBuilder()
                .setName(CommonLogger.FILE_APPENDERS[0])
                .withFileName(logFilePath)
                .setLayout(layout).build();

        return initialize ? initializeAppender(logger, fileAppender, logger.logFileLevel) : fileAppender;
    }

    public static String getLogFileName(AbstractOutputStreamAppender appender) {
        return ((FileManager)appender.getManager()).getFileName();
    }

    /**
     * <p>Called from {@code CommonLogger} wrapper constructor.</p>
     * It will always return a valid instance of {@code ConsoleAppender}.
     *
     * @param logger wrapper instance to get info from
     * @return the existing appender instance from {@code LoggerConfig} or
     * a newly created and initialized appender instance if none already exist.
     */
    public static AppenderData getOrInitConsoleAppender(CommonLogger logger) {

        AppenderData consoleAppenderData = findAppender(CommonLogger.CONSOLE_APPENDERS, logger.loggerConfig);
        if (consoleAppenderData == null)
        {
            CommonLogger.LOGGER.warn("Unable to find reachable console appender in LoggerConfig");
            Appender consoleAppender = findAppender(CommonLogger.CONSOLE_APPENDERS, logger.config);

            return new AppenderData(logger.loggerConfig, consoleAppender != null ?
                            initializeAppender(logger, consoleAppender, logger.logLevel) :
                            createNewConsoleAppender(logger, logger.config, true), logger.logLevel);
        }
        else return consoleAppenderData;
    }

    /**
     * <p>Called from {@code CommonLogger} wrapper constructor.</p>
     * It will always return a valid instance of {@code FileAppender}.
     *
     * @param logger wrapper instance to get info from
     * @param consoleLayout logging format the appender will use
     * @param logFilePath path to the logfile the appender will print to
     * @return the existing appender instance from {@code LoggerConfig} or
     * a newly created and initialized appender instance if none already exist.
     */
    public static AppenderData<AbstractOutputStreamAppender> getOrInitFileAppender(
            CommonLogger logger, Layout<? extends Serializable> consoleLayout, String logFilePath) {

        AppenderData<AbstractOutputStreamAppender> fileAppenderData =
                findFileAppender(CommonLogger.FILE_APPENDERS, logger.loggerConfig);

        if (fileAppenderData == null)
        {
            CommonLogger.LOGGER.warn("Unable to find reachable file appender in LoggerConfig");
            AbstractOutputStreamAppender fileAppender = findFileAppender(CommonLogger.FILE_APPENDERS, logger.config);

            return new AppenderData<>(logger.loggerConfig, fileAppender != null ?
                            initializeAppender(logger, fileAppender, logger.logLevel) :
                            createNewFileAppender(logger, consoleLayout, logFilePath, true), logger.logLevel);
        }
        else return fileAppenderData;
    }

    /**
     * Add the appender to supplied {@code LoggerConfig} and activate it.
     *
     * @param logger wrapper instance to get {@code LoggerConfig} from
     * @param level logging Level the appender will operate under
     * @param <T> appender object type
     * @return the activated appender instance
     */
    public static <T extends Appender> T initializeAppender(CommonLogger logger, T appender, Level level) {

        String appenderClass = appender.getClass().getSimpleName();
        CommonLogger.LOGGER.debug("Initializing %s for logger %s", appenderClass, logger.name);

        logger.loggerConfig.addAppender(appender, level, null);

        if (!appender.isStarted())
            appender.start();

        return appender;
    }

    /**
     * Update logger appender by removing the existing appender
     * and adding an updated version of the appender to the {@code LoggerConfig}.
     *
     * @param logger wrapper instance updating the appender
     * @param appender instance of the appender we are updating
     * @param level {@code log4j} level we want to update the appender to
     * If no such class type was found an error will be printed.
     */
    public static void updateAppender(LoggerConfig loggerConfig, LoggerContext context, Appender appender, Level level, @Nullable Filter filter) {

        if (findAppender(new String[]{appender.getName()}, loggerConfig) != null)
        {
            CommonLogger.LOGGER.debug("Updating %s %s in LoggerConfig %s to level %s",
                    appender.getClass().getSimpleName(), appender.getName(), loggerConfig.getName(), level);

            loggerConfig.removeAppender(appender.getName());
            loggerConfig.addAppender(appender, level, filter);
            context.updateLoggers();
        }
        else CommonLogger.LOGGER.error("Trying to update non-existing %s %s in LoggerConfig %s!",
                appender.getClass().getSimpleName(), appender.getName(), loggerConfig.getName());
    }
    public static void updateAppender(LoggerConfig loggerConfig, LoggerContext context, Appender appender, Level level) {
        updateAppender(loggerConfig, context, appender, level, null);
    }

    /**
     * Search inside the logger {@code Configuration} file to find the existing {@code LoggerConfig}
     * or create a new default one with the appropriate name and level if none was found.
     *
     * @param logger {@code CommonLogger} we are getting or creating the {@code LoggerConfig} for
     * @return the current {@code LoggerConfig} for the name specified in {@code CommonLogger}
     * or the root logger configuration if no existing {@code LoggerConfig} was found
     */
    public static LoggerConfig getOrCreateLoggerConfig(CommonLogger logger, boolean additive) {

        if (!logger.config.getLoggers().containsKey(logger.name))
        {
            CommonLogger.LOGGER.debug("Creating new LoggerConfig for %s in current context", logger.name);

            /* includeLocation - whether location should be passed downstream
             * Not quite sure what this parameter does so just set it to null...
             */
            String includeLoc = null;
            //noinspection ConstantConditions
            LoggerConfig loggerConfig = LoggerConfig.createLogger(additive, logger.logLevel,
                    logger.name, includeLoc, new AppenderRef[]{},null, logger.config, null);

            logger.config.addLogger(logger.name, loggerConfig);
            return loggerConfig;
        }
        else {
            CommonLogger.LOGGER.debug("Getting LoggerConfig %s from current context", logger.name);
            return logger.config.getLoggerConfig(logger.name);
        }
    }

    /**
     * @return  the first appender entry with the supplied name found in
     * {@code Configuration} or {@code null} if no entry was found.
     */
    public static @Nullable <T extends Appender> AppenderData<T> findAppender(String[] names, final LoggerConfig loggerConfig, Class<T> clazz) {

        LoggerConfig lookupConfig = loggerConfig;

        for (String name : names) {
            while (lookupConfig != null)
            {
                AppenderControlArraySet appenderCtrls = getAppenderControls(lookupConfig);
                for (AppenderControl control : appenderCtrls.get())
                {
                    Appender appender = control.getAppender();
                    if (appender.getName().equalsIgnoreCase(name) && clazz.isAssignableFrom(appender.getClass()))
                        return new AppenderData<>(lookupConfig, appender, getAppenderLevel(control));
                }
                lookupConfig = lookupConfig.isAdditive() ? lookupConfig.getParent() : null;
            }
            /* Reset the search entry point before next name lookup */
            lookupConfig = loggerConfig;
        }
        return null;
    }
    public static @Nullable AppenderData findAppender(String[] names, LoggerConfig loggerConfig) {
        return findAppender(names, loggerConfig, Appender.class);
    }
    public static @Nullable AppenderData<AbstractOutputStreamAppender> findFileAppender(String[] names, LoggerConfig loggerConfig) {
        return findAppender(names, loggerConfig, AbstractOutputStreamAppender.class);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <T extends Appender> T findAppender(String[] names, Configuration config, Class<T> clazz) {

        for (String name : names)
        {
            for (java.util.Map.Entry<String, Appender> entry : config.getAppenders().entrySet()) {
                if (name.equalsIgnoreCase(entry.getKey()) && clazz.isAssignableFrom(entry.getValue().getClass()))
                    return (T) entry.getValue();
            }
        }
        return null;
    }
    public static @Nullable Appender findAppender(String[] names, Configuration config) {
        return findAppender(names, config, Appender.class);
    }
    public static @Nullable AbstractOutputStreamAppender findFileAppender(String[] names, Configuration config) {
        return findAppender(names, config, AbstractOutputStreamAppender.class);
    }

    public static javafx.util.Pair<AppenderRef, LoggerConfig> findAppenderRef(String[] refs, LoggerConfig loggerConfig) {

        LoggerConfig lookupConfig = loggerConfig;

        for (String ref : refs) {
            while (lookupConfig != null)
            {
                for (AppenderRef appenderRef : lookupConfig.getAppenderRefs()) {
                    if (appenderRef.getRef().equalsIgnoreCase(ref))
                        return new javafx.util.Pair<>(appenderRef, lookupConfig);
                }
                lookupConfig = lookupConfig.isAdditive() ? lookupConfig.getParent() : null;
            }
            /* Reset the search entry point before next name lookup */
            lookupConfig = loggerConfig;
        }
        return new javafx.util.Pair<>(null, null);
    }

    public static @Nullable Level getAppenderLevel(AppenderRef ref, LoggerConfig loggerConfig) {

        if (ref.getLevel() == null) {
            while (loggerConfig != null)
            {
                if (loggerConfig.getLevel() != null) {
                    return loggerConfig.getLevel();
                }
                loggerConfig = loggerConfig.isAdditive() ? loggerConfig.getParent() : null;
            }
            return null;
        }
        else return ref.getLevel();
    }

    public static @Nullable Level getAppenderLevel(AppenderControlArraySet appenderCtrls, Appender appender) throws NoSuchElementException {

        for (AppenderControl control : appenderCtrls.get()) {
            if (control.getAppenderName().equals(appender.getName()))
                return getAppenderLevel(control);
        }
        throw new NoSuchElementException();
    }
    public static @Nullable Level getAppenderLevel(AppenderControl control) {
        return ReflectionUtils.readPrivateField(control, "level", Level.class);
    }

    public static void updateAppendersForLevel(java.util.Map<AppenderData, Level> data, CommonLogger logger) {

        AppenderControlArraySet appenderCtrls = getAppenderControls(logger.loggerConfig);
        for (java.util.Map.Entry<AppenderData, Level> entry : data.entrySet())
        {
            Appender appender = entry.getKey().getAppender();
            Level entryLevel = entry.getValue();

            try {
                Level appenderLevel = Log4jUtils.getAppenderLevel(appenderCtrls, appender);
                if (appenderLevel != null) {
                    if (appenderLevel.intLevel() != entryLevel.intLevel())
                        updateAppender(logger.loggerConfig, logger.context, appender, entryLevel);
                }
            }
            catch (NoSuchElementException e1)
            {
                String log = "Unable to find appender level for %s, creating new one";
                CommonLogger.LOGGER.debug(log, appender.getName());
                boolean createdNewAppender = false;

                if (AbstractOutputStreamAppender.class.isAssignableFrom(appender.getClass())) {
                    for (String file : CommonLogger.FILE_APPENDERS) {
                        if (appender.getName().equalsIgnoreCase(file))
                        {
                            String logFile = getStandardLogFilePath(logger.getName());
                            createNewFileAppender(logger, appender.getLayout(), logFile, true);
                            createdNewAppender = true;
                        }
                    }
                    if (!createdNewAppender)
                    {
                        log = "Expected for %s to be an instance of AbstractOutputStreamAppender";
                        Exception e2 = new Exception(appender.getClass().getName());
                        CommonLogger.LOGGER.error(String.format(log, appender.getName()), e2);
                    }
                }
                else {
                    for (String console : CommonLogger.CONSOLE_APPENDERS) {
                        if (appender.getName().equalsIgnoreCase(console)) {
                            createNewConsoleAppender(logger, appender.getLayout(), logger.config, true, null);
                            createdNewAppender = true;
                        }
                    }
                    if (!createdNewAppender)
                    {
                        log = "Unable to create new appender %s, name doesn't match expected types";
                        Exception e2 = new Exception(appender.getClass().getName());
                        CommonLogger.LOGGER.error(String.format(log, appender.getName()), e2);
                    }
                }
            }
        }
    }

    public static AppenderControlArraySet getAppenderControls(LoggerConfig loggerConfig) {
        return Objects.requireNonNull(ReflectionUtils.readPrivateField(loggerConfig, "appenders", AppenderControlArraySet.class));
    }

    public static @Nullable <T extends AbstractFilterable> Property getAppenderProperty(T appender, String property) {

        for (Property prop : appender.getPropertyArray()) {
            if (prop.getName().equalsIgnoreCase(property)) {
                return prop;
            }
        }
        return null;
    }
}
