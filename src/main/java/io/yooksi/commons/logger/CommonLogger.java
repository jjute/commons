package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotEmpty;

@MethodsNotNull
@SuppressWarnings("unused")
public class CommonLogger extends ICommonLogger {

    /* Used for internal library logging */
    private static CommonLogger instance = new CommonLogger();

    /**
     * Create a new {@code CommonLogger} implementation.
     *
     * @param name the Logger name used to create a new log4j
     * @param implClass implementation class to instantiate
     * @param <T> {@link CommonLogger} subclass
     * @return newly instantiated implementation
     * @throws IllegalStateException if the class has no nullary constructor
     * or if the class or its nullary constructor is not accessible.
     */
    public static <T extends CommonLogger> T create(@NotEmpty String name, Class<T> implClass) {

        try {
            T impl = implClass.newInstance();
            impl.logger = LogManager.getLogger(name);
            instance.debug("Created new logger for %s", impl);
            return impl;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
    /**
     * Call this from an {@code ICommonLogger} implementation
     * when you need access to more logging methods.
     *
     * @return a instance of log4j logger created for this implementation
     * of {@code ICommonLogger}
     */
    public final Logger getLogger() {
        return logger;
    }
    /**
     * Internal method used by library classes for logging purposes.
     * @return default instance of {@code CommonLogger}.
     */
    public static ICommonLogger get() {
        return instance;
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
