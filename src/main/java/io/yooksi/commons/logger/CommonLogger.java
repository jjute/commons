package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public class CommonLogger extends AbsCommonLogger {

    private static final Logger INTER_LOGGER = LogManager.getLogger("ykcommons-internal");

    public final LoggerContext context;
    private final LoggerConfig loggerConfig;
    private final FileAppender fileAppender;

    private final Logger logger;
    private final Level logLevel;
    private Level logFileLevel;

    public CommonLogger(String logger, Level logLevel, Level logFileLevel) {

        INTER_LOGGER.printf(Level.DEBUG, "Initializing new CommonLogger %s " +
                "with level " + "%s(log), %s(file)", logger, logLevel, logFileLevel);

        this.logLevel = logLevel;
        this.logFileLevel = logFileLevel;

        context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        INTER_LOGGER.printf(Level.DEBUG, "Using %s Configuration", config.getName());

        boolean existsLogger = config.getLoggers().containsKey(logger);
        INTER_LOGGER.printf(Level.DEBUG, existsLogger ? "Getting logger %s from current context"
                : "Unable to find logger %s in current context, creating new one with default configs", logger);
        /*
         * If no existing logger configuration was found for the parameter name
         * then the root logger configuration will be used instead
         */
        this.logger = LogManager.getLogger(logger);
        loggerConfig = context.getConfiguration().getLoggerConfig(this.logger.getName());

        ConsoleAppender consoleAppender = Log4jUtils.getOrInitConsoleAppender(this);
        fileAppender = Log4jUtils.getOrInitFileAppender(this, consoleAppender.getLayout(), "logs/" + logger);

        loggerConfig.setAdditive(false);
        loggerConfig.setLevel(logLevel);
        /*
         * This causes all Loggers to re-fetch information from their LoggerConfig.
         * We have to call this if we want to see our changes take place
         */
        context.updateLoggers();
        /*
         * Currently used only by tests but is useful to all users
         * TODO: implement a system that creates new log files on each
         *  run and packs the ones from day before into a zipped archive
         */
        clearLogFile();
    }
    public CommonLogger(String logger, Level logLevel, boolean logToFile) {
        this(logger, logLevel, logToFile ? logLevel : Level.OFF);
    }

    public org.apache.logging.log4j.core.Logger getCoreLogger() {
        return ((org.apache.logging.log4j.core.Logger)logger);
    }
    public Level getLogLevel() {
        return logLevel;
    }

    /**
     * @return Logger level assigned to this logger's FileAppender.
     */
    public Level getLogFileLevel() {
        return logFileLevel;
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
     * Call this from an {@code AbsCommonLogger} implementation when you
     * need access to more logging methods. Can also be called from
     * utility methods when the logger instance is not exposed.
     *
     * @return an instance of {@code Log4j} logger created for this implementation
     * or the internal logger used as fallback when the logger is still initializing
     */
    public final Logger getLogger() {
        return logger != null ? logger : INTER_LOGGER;
    }

    public java.io.File getLogFile() {
        return new java.io.File(fileAppender.getFileName());
    }

    public void clearLogFile() {

        try {
            java.io.PrintWriter writer = new java.io.PrintWriter(getLogFile());
            writer.print(""); writer.close();
        }
        catch (java.io.FileNotFoundException e) {
            error("Unable to clear logfile", e);
        }
    }

    public void startLoggingToFile(Level level) {

        //loggerConfig.removeAppender(fileAppender.getName());
        loggerConfig.addAppender(fileAppender, level, null);
        //context.updateLoggers();
        logFileLevel = level;

        //debug("%s started logging to file with level %s", logger.getName(), level);
    }
    public void startLoggingToFile() {

        //loggerConfig.removeAppender(fileAppender.getName());
        loggerConfig.addAppender(fileAppender, logFileLevel, null);
        //context.updateLoggers();

        //debug("%s started logging to file with level %s", logger.getName(), logFileLevel);
    }

    public void stopLoggingToFile() {

        loggerConfig.removeAppender(fileAppender.getName());
        //context.updateLoggers();

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
