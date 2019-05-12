package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MethodsNotNull
public class CommonLogger extends ICommonLogger {

    private static final CommonLogger instance = new CommonLogger();
    private Logger logger = LogManager.getLogger(ICommonLogger.class);

    public Logger setLogger(String name) {
        return logger = LogManager.getLogger(name);
    }
    public static Logger get() {
        return CommonLogger.instance.logger;
    }
    /*
     * Short-hand methods to print longs to console.
     */
    public void info(String log) {
        logger.info(log);
    }
    public void error(String log) {
        logger.error(log);
    }
    public void error(String log, Object...args) {
        logger.printf(Level.ERROR, log, args);
    }
    public void error(String log, Throwable e) {
        logger.error(log, e);
    }
    public void warn(String log) {
        logger.warn(log);
    }
    public void debug(String log) {
        logger.debug(log);
    }
    public void debug(String format, Object...args) {
        logger.printf(Level.INFO, "DEBUG: " + format, args);
    }
    public void debug(String log, Throwable e) {
        logger.debug(log, e);
    }
}
