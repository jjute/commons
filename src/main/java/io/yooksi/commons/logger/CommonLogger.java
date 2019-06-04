package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public class CommonLogger extends AbsCommonLogger {

    private static final LoggerContext CONTEXT = getInternalContext();

    private static boolean isInitializing = true;

    /** Used for internal class logging, particularly by the class constructor */
    static final CommonLogger LOGGER = new CommonLogger();

    private final LoggerControl loggerControl;
    private final Logger logger;

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
     * @param logLevel console logging level
     */
    public CommonLogger(String name, Level logLevel, String logFilePath, Level fileLevel, boolean currentContext, boolean additive) {

        loggerControl = LoggerControl.create(name, logLevel, fileLevel, currentContext, additive).withAppenders(logFilePath);
        this.logger = loggerControl.getLogger();

        loggerControl.update();
        LOGGER.debug("Finished constructing logger");
    }

    /**
     * Overload constructor for when we don't want to log to file,
     * or when we want the file and console logging levels to be the same.
     *
     * @see #CommonLogger(String, Level, String, Level, boolean, boolean)
     */
    public CommonLogger(String logger, Level logLevel, Level logFileLevel, boolean dedicatedFile, boolean currentContext, boolean additive) {
        this(logger, logLevel, dedicatedFile ? Log4jUtils.getStandardLogFilePath(logger) : "", logFileLevel, currentContext, additive);
    }

    public CommonLogger(String logger, Level logLevel, boolean dedicatedFile, boolean currentContext, boolean additive) {
        this(logger, logLevel, dedicatedFile ? Log4jUtils.getStandardLogFilePath(logger) : "", logLevel, currentContext, additive);
    }

    public CommonLogger(String logger, Level logLevel, String logFilePath, boolean currentContext, boolean additive) {
        this(logger, logLevel, logFilePath, logLevel, currentContext, additive);
    }

    private CommonLogger() {

        this("CommonLogger", true);
        isInitializing = false;
    }

    CommonLogger(String name, boolean clearLogFile) {

        logger = CONTEXT.getLogger(name);
        loggerControl = new LoggerControl(name, CONTEXT).withAppenders("");
        if (clearLogFile) clearLogFile();
    }

    static boolean isInitializing() {
        return isInitializing;
    }

    public Level getLevel(LoggerLevels.Type type) {
        return loggerControl.getLevel(type);
    }

    private static LoggerContext getInternalContext() {

        ClassLoader cld = CommonLogger.class.getClassLoader();
        java.net.URL configPath = CommonLogger.class.getResource("/log4j2-cl.xml");
        try {
            /* It's imperative to get context with the currentContext parameter set to true
             * otherwise we risk the context becoming global and applying to other non-related loggers
             *
             * I don't understand enough about Log4J to understand why this happens but setting this
             * parameter to true seems to prevent this unwanted behavior.
             */
            return LoggerContext.getContext(cld, true, configPath.toURI());
        }
        catch (java.net.URISyntaxException e) {
            throw new IllegalStateException(e);
        }
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
        return loggerControl.getLoggerConfig();
    }

    /**
     * Call this from an {@code AbsCommonLogger} implementation
     * when you need access to more logging methods.
     *
     * @return an instance of {@code Log4j} logger created for this implementation
     * or the internal logger used as fallback when the logger is still initializing
     */
    public final Logger getLogger() {
        return logger != null ? logger : LOGGER.logger;
    }

    public java.io.File getLogFile() {
        return new java.io.File(loggerControl.getLogFilePath());
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
     * logging to file you should use one of the following methods:
     * <ul style="list-style-type:none">
     *     <li>{@link #startLoggingToFile()}</li>
     *     <li>{@link #stopLoggingToFile()}</li>
     * </ul>
     */
    public void setLogFileLevel(Level level) {
        loggerControl.updateAppender(AppenderType.FILE, level, null);
    }
    /**
     * Update the logger {@code FileAppender} level to match the
     * default logfile level for this wrapper. This method is intended
     * to be used after the logging to file has been programmatically
     * stopped or the wrapper was constructed with no file logging in mind.
     *
     * <p><i>Note that this will obviously have no effect if the {@code FileAppender}
     * is already set to operate at the wrapped logfile level.</i></p>
     *
     * @see #stopLoggingToFile()
     */
    public void startLoggingToFile() {
        loggerControl.startAppender(AppenderType.FILE);
    }

    public void stopLoggingToFile() {
        loggerControl.stopAppender(AppenderType.FILE);
    }

    public void wrap(Level level, String...logs) {

        for (String log : logs) {
            printf(level, log);
        }
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
    public void error(String format, Object...params) {
        logger.printf(Level.ERROR, format, params);
    }
    public void error(String log, Throwable t) {
        logger.error(log, t);
    }
    public <T extends Throwable> T throwing(T t) {
        return logger.throwing(t);
    }
    public void warn(String log) {
        logger.warn(log);
    }
    public void warn(String format, Object...params) {
        logger.printf(Level.WARN, format, params);
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
