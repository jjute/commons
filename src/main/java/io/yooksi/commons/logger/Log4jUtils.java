package io.yooksi.commons.logger;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Log4jUtils {

    /* Make the constructor private to disable instantiation */
    private Log4jUtils() {
        throw new UnsupportedOperationException();
    }

    public static ConsoleAppender initNewConsoleAppender(CommonLogger logger, Configuration config) {

        logger.getLogger().printf(Level.DEBUG,"Initializing new console appender for logger %s", logger.getLogger().getName());

        String pattern = PatternLayout.SIMPLE_CONVERSION_PATTERN;
        PatternLayout layout = PatternLayout.newBuilder().withPattern(pattern).withConfiguration(config).build();
        ConsoleAppender consoleAppender = ConsoleAppender.createDefaultAppenderForLayout(layout);

        logger.getLoggerConfig().addAppender(consoleAppender, logger.getLogLevel(), null);
        consoleAppender.start(); return consoleAppender;
    }

    public static FileAppender initNewFileAppender(CommonLogger logger, Layout<? extends Serializable> consoleLayout, String logFilePath) {

        logger.getLogger().printf(Level.DEBUG,"Initializing new file appender for logger %s", logger.getLogger().getName());

        FileAppender.Builder builder = FileAppender.newBuilder().setName("File");
        builder.withFileName(logFilePath).setLayout(consoleLayout);
        FileAppender fileAppender = builder.build();

        logger.getLoggerConfig().addAppender(fileAppender, logger.getLogFileLevel(), null);
        fileAppender.start(); return fileAppender;
    }

    public static ConsoleAppender getOrInitConsoleAppender(CommonLogger logger) {

        Configuration config = logger.context.getConfiguration();
        ConsoleAppender consoleAppender = findAppender(ConsoleAppender.class, logger.getLoggerConfig());
        return consoleAppender != null ? consoleAppender : initNewConsoleAppender(logger, config);
    }

    public static FileAppender getOrInitFileAppender(CommonLogger logger, Layout<? extends Serializable> consoleLayout, String logFilePath) {

        FileAppender fileAppender = findAppender(FileAppender.class, logger.getLoggerConfig());
        return fileAppender != null ? fileAppender : initNewFileAppender(logger, consoleLayout, logFilePath);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <T extends Appender> T findAppender(Class<T> clazz, LoggerConfig config) {

        /* It's important to use an instance of LoggerConfig as opposed to an instance
         * of Configuration to search for appenders, the formet one has what we need.
         * Both are located in 'org.apache.logging.log4j.core.config' package.
         */
        java.util.Collection<Appender> appenders = config.getAppenders().values();
        for (Appender appender : appenders) {
            if (appender.getClass().equals(clazz))
                return (T) appender;
        }
        return null;
    }

    public static @Nullable <T extends AbstractFilterable> Property getAppenderProperty(T appender, String property) {

        for (Property prop : appender.getPropertyArray()) {
            if (prop.getName().equalsIgnoreCase(property)) {
                return prop;
            }
        }
        return null;
    }
}
