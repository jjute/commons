package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public class CommonLogger extends AbsCommonLogger {

    /** Used for internal class logging, particularly by the class constructor */
    private static final Logger INTER_LOGGER = LogManager.getLogger("ykcommons-inter");

    public final LoggerContext context;
    final LoggerConfig loggerConfig;
    final FileAppender logFileAppender;

    final String name; final Logger logger;
    final Level logLevel; Level logFileLevel;

    /**
     * <p>Construct a new instance of this custom Log4j wrapper.</p>
     * <ul>
     *     <li>The construction process will be logged by an internal logger.</li>
     *     at a debug level to console exclusively.
     *     <li>If either the console or file appender are missing from the
     *     configuration file in the current context it will be created
     *     with default configurations.</li>
     *     <li>When creating a logger with the same name as an existing one
     *     but different console or file log levels the existing logger will
     *     be used and the appropriate appenders will be updated.</li>
     * </ul>
     *
     * @param logger name of the {@code Log4j} logger to create or use
     * @param logLevel console logging level
     * @param logFileLevel logfile logging level
     */
    public CommonLogger(String logger, Level logLevel, Level logFileLevel) {

        INTER_LOGGER.printf(Level.DEBUG, "Initializing new CommonLogger %s " +
                "with level " + "%s(log), %s(file)", logger, logLevel, logFileLevel);

        this.name = logger;
        this.logLevel = logLevel;
        this.logFileLevel = logFileLevel;

        context = (LoggerContext) LogManager.getContext(false);
        final Configuration config = context.getConfiguration();
        final String logFilePath = "logs/" + logger + ".log";

        INTER_LOGGER.printf(Level.DEBUG, "Using %s Configuration", config.getName());

        loggerConfig = Log4jUtils.getOrCreateLoggerConfig(this);

        ConsoleAppender consoleAppender = Log4jUtils.getOrInitConsoleAppender(this);
        FileAppender fileAppender = Log4jUtils.getOrInitFileAppender(this, consoleAppender.getLayout(), logFilePath);
        /*
         * Update appenders in case we are creating a CommonLogger with
         * an already existing logger name but different logger levels.
         * Read Log4jUtils#updateAppender method for more information.
         */
        if (!loggerConfig.getLevel().equals(logLevel)) {
            Log4jUtils.updateAppender(this, consoleAppender, logLevel);
        }
        /* There is no other way to get FileAppender level but to
         * store and retrieve that information via property arrays.
         * This property is assigned in Log4jUtils#createNewFileAppender
         */
        Property property = Log4jUtils.getAppenderProperty(fileAppender, "level");
        if (property != null && !property.getValue().equals(logFileLevel.name()))
        {
            fileAppender = Log4jUtils.createNewFileAppender(this, consoleAppender.getLayout(), logFilePath);
            Log4jUtils.updateAppender(this, fileAppender, logFileLevel);
        }

        this.logFileAppender = fileAppender;
        this.logger = LogManager.getLogger(logger);

        loggerConfig.setAdditive(false);
        loggerConfig.setLevel(logLevel);

        /*
         * This causes all Loggers to re-fetch information from their LoggerConfig.
         * We have to call this if we want to see our changes take place
         */
        context.updateLoggers();
    }

    /**
     * Overload constructor for when we don't want to log to file,
     * or when we want the file and console logging levels to be the same.
     *
     * @param logToFile should we enable or disable logging to file
     * @see #CommonLogger(String, Level, Level)
     */
    public CommonLogger(String logger, Level logLevel, boolean logToFile) {
        this(logger, logLevel, logToFile ? logLevel : Level.OFF);
    }

    public org.apache.logging.log4j.core.Logger getCoreLogger() {
        return ((org.apache.logging.log4j.core.Logger)logger);
    }

    /**
     * @return Logger level assigned to this logger's ConsoleAppender.
     */
    public Level getLogLevel() {
        return logLevel;
    }
    /**
     * @return Logger level assigned to this logger's FileAppender.
     */
    public Level getLogFileLevel() {
        return logFileLevel;
    }
    public String getName() {
        return name;
    }

    /**
     * <p>{@code LoggerConfigs} are defined as {@code <logger>} blocks in the {@code log4j2.xml}
     * {@code Configuration} file and are defined for each dedicated logger name.</p>
     *
     * If no {@code LoggerConfig} dedicated configuration definition was found
     * in the XML file for our logger the root logger config will be used instead.
     *
     * @return dedicated logger configuration entry from {@code log4j2.xml} or root
     * configuration entry if no dedicated config for our logger was found.
     */
    public LoggerConfig getLoggerConfig() {
        return loggerConfig;
    }

    /**
     * Call this from an {@code AbsCommonLogger} implementation
     * when you need access to more logging methods.
     *
     * @return an instance of {@code Log4j} logger created for this implementation
     * or the internal logger used as fallback when the logger is still initializing
     */
    public final Logger getLogger() {
        return logger != null ? logger : INTER_LOGGER;
    }

    public java.io.File getLogFile() {
        return new java.io.File(logFileAppender.getFileName());
    }

    public void clearLogFile() {
        /*
         * Currently used only by tests but is useful to all users
         * TODO: implement a system that creates new log files on each
         *  run and packs the ones from day before into a zipped archive
         */
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(getLogFile());
            writer.print(""); writer.close();
        }
        catch (java.io.FileNotFoundException e) {
            error("Unable to clear logfile", e);
        }
    }

    /**
     * Update the logger {@code FileAppender} level to match
     * the method parameter level. If all you want is to start or stop
     * logging to file you should use of these respective methods:
     * <ul style="list-style-type:none">
     *     <li>{@link #startLoggingToFile()}</li>
     *     <li>{@link #stopLoggingToFile()}</li>
     * </ul>
     * @see Log4jUtils#updateAppender(CommonLogger, Appender, Level)
     */
    public void setLogFileLevel(Level level) {

        logFileLevel = level;
        Log4jUtils.updateAppender(this, logFileAppender, level);
        //debug("%s started logging to file with level %s", logger.getName(), level);
    }
    /**
     * Update the logger {@code FileAppender} level to match the
     * default logfile level for this wrapper. This method is intended
     * to be used after the logging to file has been programatically
     * stopped or the wrapper was constructed with no file logging in mind.
     *
     * <p><i>Note that this will obviously have no effect if the {@code FileAppender}
     * is already set to operate at the wrapped logfile level.</i></p>
     *
     * @see Log4jUtils#updateAppender(CommonLogger, Appender, Level)
     * @see #stopLoggingToFile()
     */
    public void startLoggingToFile() {

        Log4jUtils.updateAppender(this, logFileAppender, logFileLevel);
        //debug("%s started logging to file with level %s", logger.getName(), logFileLevel);
    }

    public void stopLoggingToFile() {

        Log4jUtils.updateAppender(this, logFileAppender, Level.OFF);
        //debug("%s stopped logging to file with level %s", logger.getName(), logFileLevel);
    }

    @Override
    public void finalize() throws Throwable {

        info("Finalizing %s and removing logger %s",this, logger.getName());
        context.getConfiguration().removeLogger(logger.getName());
        super.finalize();
    }

    /*
     * Short-hand methods to print longs to console.
     */
    public void info(String log) {
        logger.info(log);
    }
    public void info(String format, Object...params) {
        logger.printf(Level.INFO, format, params);
    }
    public void info(String log, Throwable t) {
        logger.info(log, t);
    }
    public void error(String log) {
        logger.error(log);
    }
    public void error(String log, Object...params) {
        logger.printf(Level.ERROR, log, params);
    }
    public void error(String log, Throwable t) {
        logger.error(log, t);
    }
    public void warn(String log) {
        logger.warn(log);
    }
    public void debug(String log) {
        logger.debug(log);
    }
    public void debug(String format, Object...params) {
        logger.printf(Level.DEBUG, format, params);
    }
    public void debug(String log, Throwable t) {
        logger.debug(log, t);
    }
    final public void printf(Level level, String format, Object... params) {
        logger.printf(level, format, params);
    }
}
