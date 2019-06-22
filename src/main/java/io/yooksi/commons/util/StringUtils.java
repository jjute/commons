package io.yooksi.commons.util;

import io.yooksi.commons.define.MethodsNotNull;
import io.yooksi.commons.define.RegexPatterns;

@MethodsNotNull
@SuppressWarnings("WeakerAccess")
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * Normalize all <i>End of Line</i> control characters found in the given text
     * by replacing them with a single system dependent variant that corresponds
     * to the current operating system the user is running.
     *
     * @see System#lineSeparator()
     */
    public static String normalizeEOL(String text) {
        return RegExUtils.replaceAll(text, RegexPatterns.LINE_SEPARATOR, System.lineSeparator());
    }

    /**
     * Encapsulate the string argument with quotation marks
     * @param single whether to use single instead of double quotation marks
     */
    public static String quote(String s, boolean single) {
        char quote = single ? '\'' : '\"';
        return quote + s + quote;
    }

    /**
     * <p>Encapsulate the string representation of an object with single
     * or double quotation marks depending on the object type.</p>
     * <ul>
     *     <li>String - double quotation marks</li>
     *     <li>Number - no quotation marks</li>
     *     <li>Other - single quotation marks</li>
     * </ul>
     */
    public static String smartQuote(Object s) {

        return s instanceof Number ? formatQuotient((Number) s) :
                quote(String.valueOf(s), !(s instanceof String));
    }

    /**
     * Drop the quotient remainder if it doesn't have a value.
     * @return a string representation of the quotient
     */
    public static String formatQuotient(Number num) {

        String sNumber = String.valueOf(num);
        if (num instanceof Double || num instanceof Float) {
            String[] segments = sNumber.split("\\.");
            return Integer.valueOf(segments[1]) == 0 ? segments[0] : sNumber;
        }
        return sNumber;
    }
}
