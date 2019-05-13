package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MethodsNotNull
public abstract class ICommonLogger {

    Logger logger = LogManager.getLogger();

    abstract void info(String log);

    abstract void error(String log);

    abstract void error(String format, Object...args);

    abstract void error(String log, Throwable e);

    abstract void warn(String log);

    abstract void debug(String log);

    abstract void debug(String format, Object...args);

    abstract void debug(String log, Throwable e);

    abstract void printf(Level level, String format, Object... params);
}
