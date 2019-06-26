package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@MethodsNotNull
public abstract class ICommonLogger {

    Logger logger = LogManager.getLogger("yooksi.commons");

    abstract public void info(String log);

    abstract public void error(String log);

    abstract public void error(String format, Object...args);

    abstract void error(String log, Throwable e);

    abstract public void warn(String log);

    abstract public void debug(String log);

    abstract public void debug(String format, Object...args);

    abstract public void debug(String log, Throwable e);

    abstract public void printf(Level level, String format, Object... params);
}
