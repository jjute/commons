package io.yooksi.commons.logger;

import org.apache.logging.log4j.Level;

@SuppressWarnings("unused")
public class LoggerLevels {

    public enum Type {

        LOGGER(0), CONSOLE(1), FILE(2);

        private final int i;
        Type(int i) { this.i = i; }
    }

    private Level[] levels = new Level[Type.values().length];

    public LoggerLevels(Level logger, Level console, Level file) {

        if (console.intLevel() > logger.intLevel()) {
            CommonLogger.LOGGER.warn("Console logs (Level.%s) are filtered by LoggerConfig (Level.%s)", console, logger);
        }
        else if (file.intLevel() > logger.intLevel()) {
            CommonLogger.LOGGER.warn("File logs (Level.%s) are filtered by LoggerConfig (Level.%s)", file, logger);
        }

        levels[Type.LOGGER.i] = logger;
        levels[Type.CONSOLE.i] = console;
        levels[Type.FILE.i] = file;
    }

    public Level getLevel(AppenderType type) {
        return levels[type.getLevelType().i];
    }
    public Level getLevel(Type level) {
        return levels[level.i];
    }
    public void setLevel(Type type, Level level) {
        levels[type.i] = level;
    }
    public void setLevel(AppenderType type, Level level) {
        levels[type.getLevelType().i] = level;
    }

    @Override
    public String toString() {

        String[] logElements = new String[Type.values().length];
        for (Type t : Type.values()) {
            logElements[t.i] = t.name() + "=" + levels[t.i];
        }
        return String.join(", ", logElements);
    }
}
