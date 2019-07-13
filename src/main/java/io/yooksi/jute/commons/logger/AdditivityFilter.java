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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

/**
 * Use this filter to suppress additivity effect of another logger. The suppression degree can be controlled
 * with level parameters. For example lets say we have an additive {@code LoggerConfig} with a console appender
 * both set to {@code Level.DEBUG} while the root logger is set to {@code Level.INFO}, and we wanted our debug
 * logger to act as an override logger and print logs that are {@code Level.DEBUG} or lower. Unfortunately all
 * logs below {@code Level.DEBUG} printed by the first logger would be printed twice due to propagation. We could
 * accomplish this by disabling additivity but what if we wanted to propagate other appenders.
 */
@Plugin(name = "AdditivityFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
@PerformanceSensitive("allocation")
@SuppressWarnings({"unused", "WeakerAccess"})
public final class AdditivityFilter extends AbstractFilter {

    @PluginFactory
    public static AdditivityFilter createFilter(
            @PluginAttribute("logger") final String logger,
            @PluginAttribute("minLevel") final Level minLevel,
            @PluginAttribute("maxLevel") final Level maxLevel,
            @PluginAttribute("onMatch") final Result match,
            @PluginAttribute("onMismatch") final Result mismatch) {

        final String actualLogger = logger == null ? "" : logger;
        final Level actualMinLevel = minLevel == null ? Level.OFF : minLevel;
        final Level actualMaxLevel = maxLevel == null ? Level.ALL : maxLevel;
        final Result onMatch = match == null ? Result.DENY : match;
        final Result onMismatch = mismatch == null ? Result.NEUTRAL : mismatch;
        return new AdditivityFilter(actualLogger, actualMinLevel, actualMaxLevel, onMatch, onMismatch);
    }

    public static AdditivityFilter createFilter(String logger) {
        return createFilter(logger, null, null, null, null);
    }

    private final String logger;
    private final Level maxLevel;
    private final Level minLevel;

    private AdditivityFilter(String logger, Level min, Level max, Result onMatch, Result onMismatch) {
        super(onMatch, onMismatch);
        this.logger = logger;
        this.maxLevel = max;
        this.minLevel = min;
    }

    /*@Override
    public Result filter(final LogEvent event) {
        return event.getLoggerName().equals(this.logger) && event.getLevel()
                .isInRange(minLevel, maxLevel) ?

    }*/

    private Result filter(final Logger logger) {
        return logger.getName().equals(this.logger) && logger.getLevel() != null &&
                logger.getLevel().isInRange(minLevel, maxLevel) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
                         final Throwable t) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
                         final Throwable t) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object... params) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6,
                         final Object p7) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6,
                         final Object p7, final Object p8) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6,
                         final Object p7, final Object p8, final Object p9) {
        return filter(logger);
    }
}
