package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotEmpty;

@MethodsNotNull
public class CommonLogger extends ICommonLogger {

    // TODO: Document this map and what exactly it STORES
    private static java.util.Map<Thread, ICommonLogger> map = new java.util.HashMap<>();
    private static CommonLogger defaultInstance = new CommonLogger();

    public static <T extends  CommonLogger> T create(@NotEmpty String loggerName, Class<T> implClass) {

        try {
            T impl = implClass.newInstance();
            impl.logger = LogManager.getLogger(loggerName);
            map.put(Thread.currentThread(), impl);
            return impl;
        }
        catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
    public final Logger get() {
        return logger;
    }
    // TODO: Document this method and what exactly it DOES
    public static ICommonLogger getLogger() {
        Object object = map.get(Thread.currentThread());
        return object != null ? ((ICommonLogger)object) : defaultInstance;
    }

    /*
     * Short-hand methods to print longs to console.
     */
    public void info(String log) {
        logger.info(log);
    }
    public void info(String format, Object...params) {
        logger.info(format, params);
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
        logger.printf(Level.INFO, "DEBUG: " + format, params);
    }
    public void debug(String log, Throwable t) {
        logger.debug(log, t);
    }
    final public void printf(Level level, String format, Object... params) {
        logger.printf(level, format, params);
    }
}
