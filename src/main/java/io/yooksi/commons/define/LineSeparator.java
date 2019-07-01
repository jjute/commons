package io.yooksi.commons.define;

/**
 * Holds control character definitions that represent a line-separator for different operating systems.
 * The definitions were copied over from {@link com.sun.org.apache.xml.internal.serialize.LineSeparator LineSeparator}
 * class belonging to {@code com.sun.org.apache} package to avoid the following warning for each use:
 * <ul>
 *     {@code warning: LineSeparator is internal proprietary API and may be removed in a future release}
 * </ul>
 */
public final class LineSeparator {

    /**
     * Line separator for Unix systems (<tt>\n</tt>).
     */
    public static final String Unix = "\n";


    /**
     * Line separator for Windows systems (<tt>\r\n</tt>).
     */
    public static final String Windows = "\r\n";


    /**
     * Line separator for Macintosh systems (<tt>\r</tt>).
     */
    public static final String Macintosh = "\r";
}