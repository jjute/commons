package io.yooksi.commons.logger;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;

public final class AppenderType<T extends Appender> {

    public static final AppenderType<Appender> CONSOLE = new AppenderType<>
            (Appender.class, LoggerLevels.Type.CONSOLE, "ConsoleAppender", "CLConsole", "Console");

    public static final AppenderType<AbstractOutputStreamAppender> FILE = new AppenderType<>
            (AbstractOutputStreamAppender.class, LoggerLevels.Type.FILE, "FileAppender", "CLFile", "File");

    private final Class<T> tClass;
    private final LoggerLevels.Type tLevel;
    private final String typeName;
    private final String[] names;

    private AppenderType(Class<T> tClass, LoggerLevels.Type tLevel, String typeName, String...names) {

        this.tClass = tClass;
        this.tLevel = tLevel;
        this.typeName = typeName;
        this.names = names;
    }

    public InitializationPackage.Builder<T> getBuilder(LoggerControl control) {
        return InitializationPackage.create(control, this);
    }
    public Class<T> getTypeClass() {
        return tClass;
    }
    public LoggerLevels.Type getLevelType() {
        return tLevel;
    }
    public String[] getNames() {
        return names;
    }
    public String getPrimaryName() {
        return names[0];
    }
    @Override
    public String toString() {
        return typeName;
    }
}
