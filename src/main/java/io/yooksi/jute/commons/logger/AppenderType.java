/*
 * Copyright [2019] [Matthew Cain]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.yooksi.jute.commons.logger;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;

/**
 * Appender types hold extended information about implementation classes.
 * They can be used to infer generic types, map {@code AppenderData} objects
 * and assign appropriate levels in {@code LoggerControl}.
 *
 * @param <T> Appender implementation type
 */
public final class AppenderType<T extends Appender> {

    /**
     * Represents an appender that is used to print to console.
     */
    public static final AppenderType<Appender> CONSOLE = new AppenderType<>
            (Appender.class, LoggerLevels.Type.CONSOLE, "ConsoleAppender", "CLConsole", "Console");

    /**
     * Represents an appender that is used to print to a log file.
     */
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

    /**
     * Helper method to create a package builder for this type.
     * @see InitializationPackage#create(LoggerControl, AppenderType)
     */
    InitializationPackage.Builder<T> getBuilder(LoggerControl control) {
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
