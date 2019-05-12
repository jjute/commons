package io.yooksi.commons.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;

public abstract class ICommonLogger {

    Logger logger = LogManager.getLogger(ICommonLogger.class);

    abstract void info(String log);

    abstract void error(String log);

    abstract void error(String format, Object...args);

    abstract void error(String log, Throwable e);

    abstract void warn(String log);

    abstract void debug(String log);

    abstract void debug(String format, Object...args);

    abstract void debug(String log, Throwable e);
}
