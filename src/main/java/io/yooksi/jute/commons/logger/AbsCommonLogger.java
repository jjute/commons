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
