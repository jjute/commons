/*
 * Copyright [2019] [Matthew Cain]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.yooksi.jute.commons.logger;

import io.yooksi.jute.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.*;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public class CommonLogger extends AbsCommonLogger {

    /** Name of the internal log4j2 configuration file. */
    private static final String CONFIG_FILE = "log4j2-cl.xml";

    /** Internal log4j2 context for the current classpath. */
    private static final LoggerContext CONTEXT = getInternalContext();

    /**
     * <p>Will be set to {@code false} after the internal logger is done constructing.</p>
     * Useful for when we want to check if we are able to use the internal logging yet.
     */
    private static boolean isInitializing = true;

    /** Used for internal class logging, particularly by the class constructor */
    static final CommonLogger LOGGER = new CommonLogger();

    private final LoggerControl loggerControl;
    private final Logger logger;

    /* Internal constructor used only by LOGGER var */
    private CommonLogger() {

        this("CommonLogger", true);
        isInitializing = false;
    }

    /**
     * Constructor for internal loggers <b>ONLY</b>.
     * @param clearLogFile should the logfile be cleared after construction
     */
    CommonLogger(String name, boolean clearLogFile) {

        logger = CONTEXT.getLogger(name);
        loggerControl = new LoggerControl(name, CONTEXT).withAppenders("");
        if (clearLogFile) clearLogFile();
    }

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
     * @param name {@code LoggerConfig} name to find or create
     * @param logLevel console logging level
     * @param logFilePath path to the dedicated log file we want to output logging to.
     *                    An empty string will disable logging to a dedicated file.
     * @param fileLevel file logging level
     * @param currentContext whether to use an internal or external context, with internal context being tied
     *                       to our local {@code log4j2} xml file and defined in {@link #CONTEXT}
     * @param additive should log events be propagated to {@code LoggerConfig} parents.
     * @see LoggerControl#create(String, Level, Level, boolean, boolean)
     */
    public CommonLogger(String name, Level logLevel, String logFilePath, Level fileLevel, boolean currentContext, boolean additive) {

        loggerControl = LoggerControl.create(name, logLevel, fileLevel, currentContext, additive).withAppenders(logFilePath);
        this.logger = loggerControl.getLogger();

        loggerControl.update();
        LOGGER.debug("Finished constructing logger");
    }

    /**
     * Overload constructor for when we don't want to log to file or
     * we want to log to a file with a standard log file path.
     *
     * @param logLevel console logging threshold level
     * @param logFileLevel file logging threshold level
     * @param dedicatedFile whether to use a dedicated logging file with standard path.
     *
     * @see #CommonLogger(String, Level, String, Level, boolean, boolean)
     * @see Log4jUtils#getStandardLogFilePath(String)
     */
    public CommonLogger(String logger, Level logLevel, Level logFileLevel, boolean dedicatedFile, boolean currentContext, boolean additive) {
        this(logger, logLevel, dedicatedFile ? Log4jUtils.getStandardLogFilePath(logger) : "", logFileLevel, currentContext, additive);
    }

    /**
     * Overload constructor for when we don't want to log to file or when we want
     * the file and console logging levels to be the same and are too lazy to explicitly write so.
     *
     * @param logLevel logging level to use for both console and file
     * @param dedicatedFile whether to use a dedicated logging file with standard path.
     *
     * @see #CommonLogger(String, Level, String, Level, boolean, boolean)
     * @see Log4jUtils#getStandardLogFilePath(String)
     */
    public CommonLogger(String logger, Level logLevel, boolean dedicatedFile, boolean currentContext, boolean additive) {
        this(logger, logLevel, dedicatedFile ? Log4jUtils.getStandardLogFilePath(logger) : "", logLevel, currentContext, additive);
    }

    /**
     * Overload constructor for when we want to log to a dedicated file with a
     * specific path and use the same logging level for both console and file
     *
     * @param logLevel logging level to use for both console and file
     * @param logFilePath dedicated log file path to output to
     *
     * @see #CommonLogger(String, Level, String, Level, boolean, boolean)
     */
    public CommonLogger(String logger, Level logLevel, String logFilePath, boolean currentContext, boolean additive) {
        this(logger, logLevel, logFilePath, logLevel, currentContext, additive);
    }

    /**
     * @return a {@code LoggerContext} for our internal {@link #CONFIG_FILE}.
     *
     * @throws IllegalStateException if the return value from {@link Class#getResource(String)}
     * is {@code null} which means the internal configuration file was not found or the value
     * is not formatted strictly according to RFC2396 and cannot be converted to a URI.
     */
    private static LoggerContext getInternalContext() throws IllegalStateException {

        ClassLoader cld = CommonLogger.class.getClassLoader();
        java.net.URL configPath = CommonLogger.class.getResource("/" + CONFIG_FILE);
        try {
            if (configPath == null) {
               throw new IllegalStateException("Unable to find internal log4j2 configuration file.");
            }
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

    static boolean isInitializing() {
        return isInitializing;
    }

    public Level getLevel(LoggerLevels.Type type) {
        return loggerControl.getLevel(type);
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

    /**
     * @return a log {@code File} instance from file appender output path.
     *        <i>Note that the return value is <b>not guaranteed</b> to be an existing file.</i>
     */
    public java.io.File getLogFile() {
        return new java.io.File(loggerControl.getLogFilePath());
    }

    /**
     * Deletes the contents of a log file used by a registered file appender.
     * @see #getLogFile()
     */
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
     * Update the logger {@code FileAppender} level to match the method parameter level.
     * To simply start or stop logging to log-file consider using these methods:
     *
     * @see #startLoggingToFile()
     * @see #stopLoggingToFile()
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

    /**
     * Removes a registered {@link AppenderType#FILE} from the current {@code LoggerConfig}.
     * @see LoggerControl#stopAppender(AppenderType)
     */
    public void stopLoggingToFile() {
        loggerControl.stopAppender(AppenderType.FILE);
    }


    /**
     * Print each given log as separate log event with the provided level.
     */
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
