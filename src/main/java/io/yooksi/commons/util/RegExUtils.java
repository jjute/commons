package io.yooksi.commons.util;

import io.yooksi.commons.define.MethodsNotNull;

import java.util.List;
import java.util.regex.Matcher;

@MethodsNotNull
@SuppressWarnings("unused")
public class RegExUtils extends org.apache.commons.lang3.RegExUtils {

    public static String[] collectMatches(Matcher matcher) {

        List<String> matches = new java.util.ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches.toArray(new String[0]);
    }
}
