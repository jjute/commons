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
     * @param application name of the application to retrieve the path for
     * @param fullSearch search through <b>all</b> environment variables
     * @return path for a given application name
     *
     * @throws IllegalStateException if the {@code Path} environment variable was not found.
     */
    public static @Nullable Path getApplicationPath(String application, boolean fullSearch) {

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
                   FilenameFilter filter = new NameFileFilter(application);
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
               for (java.util.Map.Entry<String, String> e : System.getenv().entrySet())
               {
                   String path = e.getValue();
                   if (path.equals(application)) {
                       return java.nio.file.Paths.get(path);
                   }
               }
           }
           return null;
       }
    }
}
