package io.yooksi.commons.define;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;

import java.util.regex.Pattern;

/**
 * This class contains compiled regex patterns for general use.
 * @see io.yooksi.commons.util.RegExUtils
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
}
