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
package io.yooksi.jute.commons;

import io.yooksi.jute.commons.define.LineSeparator;
import io.yooksi.jute.commons.define.RegExPatterns;
import io.yooksi.jute.commons.util.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class RegExTests {

    @Test
    public void testSimpleVersionNumberRegEx() {

        simpleVersionNumberPatternShouldFindAMatch("0", "0");
        simpleVersionNumberPatternShouldFindAMatch("010.98", "010", "98");

        simpleVersionNumberPatternShouldFindAMatch("0.1.2.3", "0", "1", "2", "3");
        simpleVersionNumberPatternShouldFindAMatch("0.12.3344.2.1", "0", "12", "3344", "2", "1");

        simpleVersionNumberPatternShouldNotFindMatch("8.34.-1");    // Contains negative digits
        simpleVersionNumberPatternShouldNotFindMatch("0 23.42");    // Contains white-spaces
        simpleVersionNumberPatternShouldNotFindMatch("1.2..3.4");   // Contains consecutive period symbols
        simpleVersionNumberPatternShouldNotFindMatch(".2.58.6");    // Starts with a period symbol
        simpleVersionNumberPatternShouldNotFindMatch("64#23.4");    // Contains special characters

        String versionNumber = "0.1.2.3";
        Assertions.assertTrue(RegExPatterns.SIMPLE_VERSION_NUMBER.matcher(versionNumber).find());
        Assertions.assertArrayEquals(new String[] { "0", "1", "2", "3" }, versionNumber.split("\\."));
    }

    @TestOnly
    private void simpleVersionNumberPatternShouldFindAMatch(String version, String... expectedGroups) {

        Assertions.assertTrue(RegExPatterns.SIMPLE_VERSION_NUMBER.matcher(version).find());
        Assertions.assertArrayEquals(expectedGroups, version.split("\\."));
    }

    @TestOnly
    private void simpleVersionNumberPatternShouldNotFindMatch(String version) {
        Assertions.assertFalse(RegExPatterns.SIMPLE_VERSION_NUMBER.matcher(version).find());
    }

    @Test
    public void testEOLNormalization() {

        String windows = StringUtils.normalizeEOL("Sample text" + LineSeparator.Windows);
        String unix = StringUtils.normalizeEOL("Sample text" + LineSeparator.Unix);
        String mac = StringUtils.normalizeEOL("Sample text" + LineSeparator.Macintosh);

        Assertions.assertEquals(windows, unix);
        Assertions.assertEquals(windows, mac);
        Assertions.assertEquals(unix, mac);
    }

    @Test
    public void testUnixNamingConvention() {

        java.util.Map<String, Boolean> results = new java.util.HashMap<>();

        results.put("abcd", true); results.put("ab_cd", true);
        results.put("ab-cd", true); results.put("abc.d.ef", true);

        results.put("-abc", false); results.put("_abc", false);
        results.put(".-abc", false); results.put("ab$c", false);
        results.put(" abc", false); results.put("a bc", false);

        results.forEach((s, b) -> Assertions.assertEquals(
                b, RegExPatterns.UNIX_NAMING_CONVENTION.matcher(s).find()));
    }
}
