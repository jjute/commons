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
@SuppressWarnings({"unused", "WeakerAccess"})
public class FileUtils {

    /**
     * Construct and return a {@code Set} populated with {@code Path} objects
     * that represent files from all directory levels of a given path,
     * optionally excluding files with given filenames.
     *
     * @param dir starting directory to start walking the file tree from
     * @param relativize should file paths in the return value be relative to starting
     *                   directory as opposed to being absolute.
     * @param excludeFiles files to exclude from the return value
     * @return {@code Set} of files contained within a searched directory tree
     *
     * @throws IOException if an I/O error is thrown when accessing the starting file
     *
     * @see Files#walk(Path, FileVisitOption...)
     * @see #relativizePaths(Path, Set)
     */
    public static Set<Path> getDirectoryTree(Path dir, boolean relativize, String...excludeFiles) throws IOException {

        Set<Path> ignoreSet = new java.util.HashSet<>(resolvePaths(dir, excludeFiles));

        Set<Path> dirTree = ignoreSet.isEmpty() ?
                Files.walk(dir).filter(p -> Files.isRegularFile(p)).collect(Collectors.toSet()) :
                Files.walk(dir).filter(p -> Files.isRegularFile(p) && !doesPathMatch(p, ignoreSet)).collect(Collectors.toSet());

        return relativize ? relativizePaths(dir, dirTree) : dirTree;
    }

    /**
     * Check if the given path matches to <b>at least</b> one path in the
     * provided collection. When comparing the mentioned paths a condition
     * is considered a match when either of the following conditions are true:
     * <ul>
     *     <li>File or directory names denoted by two paths are identical.</li>
     *     <li>Compared paths start with the same name elements.</li>
     * </ul>
     * <i>
     *     Note that if the given path is absolute, it will be converted to a relative path.
     *      This also means that all potential match candidates have to be relative paths as well.
     * </i>
     * @param path main path to match with other paths
     * @param collection {@code Set} of paths to match with main path
     *
     * @return {@code true} if any of the conditions listed above is matched,
     *          but will return {@code false} if all paths in the collection that would
     *          normally be a match in their relative state are formed as absolute paths.
     *
     * @see Path#startsWith(Path)
     */
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
     * Resolve the array of paths against the given base path. In simpler terms
     * we are creating copies of the given base path and separately appending each
     * path from the array to the base path and returning the joined paths in a list.
     * This is the opposite of relativizing paths which displaces paths instead of joining them.
     *
     * @param base the {@code Path} to resolve against
     * @param paths an array of paths to join with base path
     * @return a {@code List} of paths that have been joined with the base path.
     *
     * @see Path#resolve(Path)
     */
    public static java.util.List<Path> resolvePaths(Path base, String[] paths) {

        java.util.List<Path> joined = new java.util.ArrayList<>();
        Arrays.stream(paths).forEach(p -> joined.add(base.resolve(Paths.get(p))));
        return joined;
    }

    /**
     * Construct and return a {@code Set} of relative paths in relation to the given base path.
     * Note that relative paths cannot be constructed if not all of the given paths have root components.
     *
     * @param base construct in relation to this {@code Path}
     * @param paths {@code Set} of paths to relativize
     * @return a {@code Set} of paths relativized in relation to base path
     *
     * @see Path#relativize(Path)
     */
    public static Set<Path> relativizePaths(Path base, Set<Path> paths) {

        Set<Path> relativized = new java.util.HashSet<>();
        paths.forEach(path -> relativized.add(base.relativize(path)));
        return relativized;
    }

    /**
     * Remove the root component of the given absolute {@code Path} and return the new relative path.
     * If the given path does not contain a root component then the same path object will be returned.
     *
     * @return relative representation of the given path or the same path object passed
     *         as method argument if the given path does not have a root component.
     */
    public static Path convertToRelativePath(Path path) {

        Path root = path.getRoot();
        return root != null ? root.relativize(path) : path;
    }

    /**
     * Filter through given paths and return an array of paths
     * that <b>doe not</b> represent valid directories.
     *
     * @see Files#isDirectory(Path, LinkOption...)
     */
    public static Path[] getInvalidDirectories(Path dir, Path...other) {

        final Path[] paths = ArrayUtils.add(other, dir);
        Set<Path> directories = new java.util.HashSet<>(Arrays.asList(paths));
        return directories.stream().filter(d -> !Files.isDirectory(d)).distinct().toArray(Path[]::new);
    }

    /**
     * @param dir directory the files are located in
     * @param extRegex file extension regular expression
     * @return an array of files located in a directory under given path,
     *         whose extension is matched by the given regular expression.
     *
     * @throws NotDirectoryException if the abstract pathname does not denote a directory,
     *                               or an unknown I/O error occurred while listing files.
     * @throws PatternSyntaxException if regular expression's syntax is invalid.
     * @throws IOException if an unknown exception occurred while listing files.
     */
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

    /**
     * @param dir directory the files are located in
     * @return an array of text files located in a directory under given path.
     *
     * @throws NotDirectoryException if the abstract pathname does not denote a directory,
     *                               or an unknown I/O error occurred while listing files.
     * @throws IOException if an unknown exception occurred while listing files.
     *
     * @see #getFilesInDirectory(Path, String)
     */
    public static java.io.File[] getTextFilesInDirectory(Path dir) throws NotDirectoryException, IOException {
        return getFilesInDirectory(dir, "txt");
    }


    /**
     * Trim leading whitespaces from each line of a text based file.
     * Whitespaces are detected using a simple regular expression that also includes tabs.
     *
     * @param file {@code File} object to process
     * @throws IOException if an I/O error occurred while reading or writing to file
     *
     * @see org.apache.commons.io.FileUtils#readLines(File, String)
     * @see org.apache.commons.io.FileUtils#writeLines(File, Collection)
     */
    public static void trimTrailingSpaceFromTextFile(java.io.File file) throws IOException {

        java.util.List<String> newLines = new java.util.ArrayList<>();
        java.util.List<String> lines = org.apache.commons.io.FileUtils.readLines(file, Charset.defaultCharset());
        lines.forEach(l -> newLines.add(l.replaceAll("[\\t\\s]+$", "")));
        org.apache.commons.io.FileUtils.writeLines(file, newLines);
    }
}
