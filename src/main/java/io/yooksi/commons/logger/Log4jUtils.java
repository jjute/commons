package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Log4jUtils {

    /* Make the constructor private to disable instantiation */
    private Log4jUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new {@code ConsoleAppender} instance with default {@code PatternLayout}
     * for the supplied {@code Configuration}.
     */
    public static ConsoleAppender createNewConsoleAppender(CommonLogger logger, Configuration config) {

        logger.getLogger().printf(Level.DEBUG, "Creating new ConsoleAppender for logger %s", logger.name);

        String pattern = PatternLayout.SIMPLE_CONVERSION_PATTERN;
        PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).withConfiguration(config).build();
        return ConsoleAppender.createDefaultAppenderForLayout(layout);
    }

    /**
     * Create a new {@code FileAppender} instance with the supplied {@code Layout}.
     *
     * @param consoleLayout logging format the appender will use
     * @param logFilePath path to the logfile the appender will print to
     */
    public static FileAppender createNewFileAppender(CommonLogger logger, Layout<? extends Serializable> consoleLayout, String logFilePath) {

        logger.getLogger().printf(Level.DEBUG, "Creating new FileAppender for logger %s", logger.name);

        Property[] properties = { Property.createProperty("level", logger.logFileLevel.name()) };
        return FileAppender.newBuilder().setName("LogFile").withFileName(logFilePath)
                .setLayout(consoleLayout).setPropertyArray(properties).build();
    }

    /**
     * <p>Called from {@code CommonLogger} wrapper constructor.</p>
     * It will always return a valid instance of {@code ConsoleAppender}.
     *
     * @param logger wrapper instance to get info from
     * @return the existing appender instance from {@code LoggerConfig} or
     * a newly created and initialized appender instance if none already exist.
     */
    public static ConsoleAppender getOrInitConsoleAppender(CommonLogger logger) {

        ConsoleAppender consoleAppender = findAppender(ConsoleAppender.class, logger.loggerConfig);
        return consoleAppender != null ? consoleAppender : initializeAppender(logger,
                createNewConsoleAppender(logger, logger.context.getConfiguration()), logger.logLevel);
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
    public static FileAppender getOrInitFileAppender(CommonLogger logger, Layout<? extends Serializable> consoleLayout, String logFilePath) {

        FileAppender fileAppender = findAppender(FileAppender.class, logger.loggerConfig);
        return fileAppender != null ? fileAppender : initializeAppender(logger,
                createNewFileAppender(logger, consoleLayout, logFilePath), logger.logFileLevel);
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
        logger.getLogger().printf(Level.DEBUG, "Initializing new %s for logger %s", appenderClass, logger.name);

        logger.loggerConfig.addAppender(appender, level, null);
        appender.start(); return appender;
    }

    /**
     * Update logger appender by removing the existing appender
     * and adding an updated version of the appender to the {@code LoggerConfig}.
     *
     * @param logger wrapper instance updating the appender
     * @param appender instance of the appender we are updating
     * @param level {@code log4j} level we want to update the appender to
     * @param <T> appender class to search for in the {@code LoggerConfig}.
     * If no such class type was found an error will be printed.
     */
    public static <T extends Appender> void updateAppender(CommonLogger logger, T appender, Level level) {

        LoggerConfig loggerConfig = logger.loggerConfig;
        if (findAppender(appender.getClass(), loggerConfig) != null)
        {
            logger.getLogger().printf(Level.DEBUG, "Updating %s %s in LoggerConfig %s to level %s",
                    appender.getClass().getSimpleName(), appender.getName(), loggerConfig.getName(), level);

            loggerConfig.removeAppender(appender.getName());
            loggerConfig.addAppender(appender, level, null);
            logger.context.updateLoggers();
        }
        else logger.getLogger().printf(Level.ERROR, "Trying to update non-existing %s %s in LoggerConfig %s!",
                appender.getClass().getSimpleName(), appender.getName(), loggerConfig.getName());
    }

    /**
     * Search inside the logger {@code Configuration} file to find the existing {@code LoggerConfig}
     * or create a new default one with the appropriate name and level if none was found.
     *
     * @param logger {@code CommonLogger} we are getting or creating the {@code LoggerConfig} for
     * @return the current {@code LoggerConfig} for the name specified in {@code CommonLogger}
     * or the root logger configuration if no existing {@code LoggerConfig} was found
     */
    public static LoggerConfig getOrCreateLoggerConfig(CommonLogger logger) {

        Configuration config = logger.context.getConfiguration();
        if (!config.getLoggers().containsKey(logger.name))
        {
            String log = "Creating new LoggerConfig for %s in current context";
            logger.getLogger().printf(Level.DEBUG, log, logger.name);
            /*
             * Don't assign levels here as it will serve no purpose if we are not using the xml file.
             * This should be done when adding new appenders in #initializeAppender method.
             */
            AppenderRef[] refs = new AppenderRef[] {
                    AppenderRef.createAppenderRef("Console",null/*logger.logLevel*/, null),
                    AppenderRef.createAppenderRef("LogFile",null/*logger.logFileLevel*/, null)
            };
            /* includeLocation - whether location should be passed downstream
             * Not quite sure what this parameter does so just set it to null...
             */
            String includeLoc = null;
            //noinspection ConstantConditions
            LoggerConfig loggerConfig = LoggerConfig.createLogger(false, logger.logLevel,
                    logger.name, includeLoc, refs,null, config, null);

            config.addLogger(logger.name, loggerConfig);
            return loggerConfig;
        }
        else {
            logger.getLogger().printf(Level.DEBUG, "Getting LoggerConfig %s from current context", logger.name);
            return config.getLoggerConfig(logger.name);
        }
    }

    /**
     * @return  the first appender entry with the supplied {@code Class} found in
     * {@code LoggerConfig} or {@code null} if no entry was found.
     */
    @SuppressWarnings("unchecked")
    public static @Nullable <T extends Appender> T findAppender(Class<T> clazz, LoggerConfig config) {

        /* It's important to use an instance of LoggerConfig as opposed to an instance
         * of Configuration to search for appenders, the formet one has what we need.
         * Both are located in 'org.apache.logging.log4j.core.config' package.
         */
        java.util.Collection<Appender> appenders = config.getAppenders().values();
        for (Appender appender : appenders) {
            if (appender.getClass().equals(clazz))
                return (T) appender;
        }
        return null;
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
