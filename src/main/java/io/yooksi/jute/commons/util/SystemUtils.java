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
import org.apache.commons.io.filefilter.NameFileFilter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

@MethodsNotNull
public class SystemUtils extends org.apache.commons.lang3.SystemUtils {

    /**
     * Search system environment variables and retrieve a path for a given application name.
     * The method will first search through {@code Path} environment variable to try to find the
     * application path. If unsuccessful and the {@code fullSearch} parameter is {@code true}
     * it will then search through all other available variables.
     *
     * @param filename name of the application to retrieve the path for. The value has to represent
     *                 a <i>full</i> filename that includes a valid file extension <i>(if one exists)</i>.
     * @param fullSearch search through <b>all</b> environment variables
     * @return path for a given application name
     *
     * @throws IllegalStateException if the {@code Path} environment variable was not found.
     */
    public static @Nullable Path getApplicationPath(String filename, boolean fullSearch) {

       String pathVar = getEnvironmentVariable("Path", null);
       if (pathVar == null) {
           throw new IllegalStateException("Unable to find or access system environment variable \"Path\"");
       }
       else {
           /* First attempt to find the path in environment variable "Path",
            * since this is where most system paths are stored.
            */
           for (String entry : pathVar.split(";"))
           {
               File dir = new File(entry);
               if (dir.isDirectory())
               {
                   FilenameFilter filter = new NameFileFilter(filename);
                   File[] files = dir.listFiles(filter);

                   if (files != null && files.length > 0) {
                       return files[0].toPath();
                   }
               }
           }
           /* If the path was not found under common system paths variable
            * expand the search to include all other environment variables.
            */
           if (fullSearch) {
               for (java.util.Map.Entry<String, String> entry : System.getenv().entrySet())
               {
                   try {
                       Path path = java.nio.file.Paths.get(entry.getValue());
                       Path fileName = path.getFileName();

                       if (fileName != null && fileName.toString().equals(filename)) {
                           return path;
                       }
                   }
                   catch (java.nio.file.InvalidPathException e) {
                       // The entry is not a valid path, continue the search
                   }
               }
           }
           return null;
       }
    }
}
