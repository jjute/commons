package io.yooksi.commons.logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

/**
 * Internal {@code CommonLogger} wrapper class used by other
 * library classes and methods for their logging purposes.
 * Use the static methods provided here instead of creating
 * your own instance of {@code CommonLogger}.
 */
@SuppressWarnings("unused")
public final class LibraryLogger {

    private static final CommonLogger logger = new CommonLogger("YKCommons", false);

    /* Make the constructor private to disable instantiation */
    private LibraryLogger() {
        throw new UnsupportedOperationException();
    }

    @Contract(pure = true)
    public static Logger get() {
        return logger.getLogger();
    }

    public static java.io.File getLogFile() {
        return logger.getLogFile();
    }
    public static void clearLogFile() {
        logger.clearLogFile();
    }

    /*
     * Short-hand methods to print logs to console. For more methods
     * use the static getter method to get a hold of a Logger instance.
     */
    public static void info(String log) {
        logger.info(log);
    }
    public static void info(String log, Object...params) {
        logger.info(log, params);
    }
    public static void error(String log) {
        logger.error(log);
    }
    public static void error(String log, Object...args) {
        logger.printf(Level.ERROR, log, args);
    }
    public static void error(String log, Throwable t) {
        logger.error(log, t);
    }
    public static void warn(String log) {
        logger.warn(log);
    }
    public static void debug(String log) {
        logger.debug(log);
    }
    public static void debug(String format, Object...args) {
        logger.debug(format, args);
    }
    public static void debug(String log, Throwable t) {
        logger.debug(log, t);
    }
    public static void printf(Level level, String format, Object... params) {
        logger.printf(level, format, params);
    }
}
