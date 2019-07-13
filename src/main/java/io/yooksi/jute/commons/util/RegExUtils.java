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
package io.yooksi.jute.commons.util;

import io.yooksi.jute.commons.define.MethodsNotNull;

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
