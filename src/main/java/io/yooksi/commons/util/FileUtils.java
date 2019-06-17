package io.yooksi.commons.util;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

@MethodsNotNull
@SuppressWarnings("unused")
public class FileUtils {

    public static Set<Path> getDirectoryTree(Path dir, boolean relativize, String...excludeFiles) throws IOException {

        Set<Path> ignoreSet = new java.util.HashSet<>(resolvePaths(dir, excludeFiles));

        Set<Path> dirTree = ignoreSet.isEmpty() ?
                Files.walk(dir).filter(p -> Files.isRegularFile(p)).collect(Collectors.toSet()) :
                Files.walk(dir).filter(p -> Files.isRegularFile(p) && !doesPathMatch(p, ignoreSet)).collect(Collectors.toSet());

        return relativize ? relativizePaths(dir, dirTree) : dirTree;
    }

    public static boolean doesPathMatch(Path path, Set<Path> collection) {

        path = convertToRelativePath(path);
        for (Path entry : collection)
        {
            boolean matches = path.getFileName().equals(entry.getFileName());
            boolean contains = !matches && path.startsWith(entry);

            if (matches || contains)  {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the root component of the given absolute {@code Path} and
     * return the new relative path. If the given path does not contain
     * a root component then the same path object will be returned.
     *
     * @return relative representation of the given path or the same
     *         path object passed as method argument if the given path
     *         does not have a root component.
     */
    public static java.util.List<Path> resolvePaths(Path base, String[] paths) {

        java.util.List<Path> joined = new java.util.ArrayList<>();
        Arrays.stream(paths).forEach(p -> joined.add(base.resolve(Paths.get(p))));
        return joined;
    }

    public static Set<Path> relativizePaths(Path base, Set<Path> paths) {

        Set<Path> relativized = new java.util.HashSet<>();
        paths.forEach(path -> relativized.add(base.relativize(path)));
        return relativized;
    }

    public static Path convertToRelativePath(Path path) {

        Path root = path.getRoot();
        return root != null ? root.relativize(path) : path;
    }

    public static Path[] getInvalidDirectories(Path dir, Path...other) {

        final Path[] paths = ArrayUtils.add(other, dir);
        Set<Path> directories = new java.util.HashSet<>(Arrays.asList(paths));
        return directories.stream().filter(d -> !Files.isDirectory(d)).distinct().toArray(Path[]::new);
    }

    public static java.io.File[] getFilesInDirectory(Path dir, String extRegex) throws NotDirectoryException,
                                                                                PatternSyntaxException, IOException {
        if (dir.toFile().isDirectory()) {
            String log = "Path \"%s\" does not point to a valid directory.";
            throw new NotDirectoryException(String.format(log, dir.toString()));
        }

        Pattern pattern = Pattern.compile(extRegex);
        java.io.File[] result = dir.toFile().listFiles(f -> !f.isDirectory() &&
                pattern.matcher(FilenameUtils.getExtension(f.getName())).find());

        if (result == null) {
            throw new IOException("Unknown IOException occurred while listing files.");
        }
        else return result;
    }

    public static java.io.File[] getTextFilesInDirectory(Path dir) throws NotDirectoryException, IOException {
        return getFilesInDirectory(dir, "txt");
    }

    public static void trimTrailingSpaceFromTextFile(java.io.File file) throws IOException {

        java.util.List<String> newLines = new java.util.ArrayList<>();
        java.util.List<String> lines = org.apache.commons.io.FileUtils.readLines(file, Charset.defaultCharset());
        lines.forEach(l -> newLines.add(l.replaceAll("[\\t\\s]+$", "")));
        org.apache.commons.io.FileUtils.writeLines(file, newLines);
    }
}
