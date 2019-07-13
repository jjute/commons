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
package io.yooksi.jute.commons.define;

import io.yooksi.jute.commons.util.RegExUtils;

import java.util.regex.Pattern;

/**
 * This class contains compiled regex patterns for general use.
 * @see RegExUtils
 */
@SuppressWarnings("unused")
public class RegExPatterns {

    /**
     * <h3>Matches</h3>
     * All sequences of characters separated by whitespaces.
     * <ul>
     *     <b>Quantifier:</b> Between {@code one} and {@code unlimited} times.
     * </ul>
     * <h3>Captures</h3>
     * Sequence of characters contained within quotation marks <i>(single and double)</i>.
     * <ul>
     *     <b>Groups:</b> {@code $1}
     * </ul>
     */
    public static final Pattern QUOTE_INCLUSIVE_SPLIT = Pattern.compile(
            "(?:\"((?:[^\"\\\\]|\\\\.|\\\\\\n)*)\")|('(?:[^'\\\\]|\\\\.|\\\\\\n)*')|(?:[\\S]+)"
    );
    /**
     * <h3>Matches</h3>
     * All system-dependent line separator control characters.
     * <ul>
     *     <b>Quantifier:</b> Between {@code one} and {@code unlimited} times.
     * </ul>
     * @see System#lineSeparator()
     */
    public static final Pattern LINE_SEPARATORS = Pattern.compile(
            "(" + LineSeparator.Unix + "|" + LineSeparator.Windows + "|" + LineSeparator.Macintosh + ")"
    );

    /**
     * <h3>Matches</h3>
     * <p>
     *     The very first line <b>only</b> if it represents a simple version number which is composed
     *     of a series of digits separated by single periods. The line has to conform to the following rules:
     *     <ul>
     *         <li>Does not contain letters, whitespaces, or special characters.</li>
     *         <li>Does not contain negative digits <i>(numbers less then zero).</i></li>
     *         <li>The line cannot start or end with a period.</li>
     *         <li>Each series of digits after the initial group has to be preceded with a single period.</li>
     *     </ul>
     * </p>
     * <ul>
     *     <b>Quantifier:</b> Between {@code one} and {@code unlimited} times.
     * </ul>
     */
    public static final Pattern SIMPLE_VERSION_NUMBER = Pattern.compile("^\\d+(?:\\.\\d+)*$");

    /**
     * <h3>Description</h3>
     * <p>
     *     Unix filenames typically only use alphanumeric characters <i>(mostly lower case)</i>,
     *     underscores, hyphens and periods. Other characters, such as dollar signs, percentage
     *     signs and brackets, have special meanings to the shell and can be distracting to work with.
     *     File names should never begin with a hyphen.
     * </p><br>
     * <h3>Matches</h3>
     * <p>
     *     The very first line <b>only</b> if it conforms to a <i>UNIX-style</i> naming convention.
     * <ul>
     *     <b>Quantifier:</b> All matching characters between {@code one} and {@code unlimited} times.
     * </ul>
     * @see <a href="http://www.linfo.org/file_name.html">File Naming Conventions in Linux</a>
     */
    public static final Pattern UNIX_NAMING_CONVENTION = Pattern.compile("^[A-Za-z0-9]+[A-Za-z0-9._-]*$");
}
