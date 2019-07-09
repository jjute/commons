package io.yooksi.jute.commons.logger;

import io.yooksi.jute.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;

@MethodsNotNull
@SuppressWarnings("unused")
abstract class AbsCommonLogger {

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
