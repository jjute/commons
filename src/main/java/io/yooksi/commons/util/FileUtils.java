package io.yooksi.commons.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FileUtils {

    public static Set<Path> getDirectoryTree(Path dir, boolean relativize, String...excludeFiles) throws IOException {

        final Path[] pIgnoreList = joinPaths(dir, excludeFiles);
        Set<Path> ignoreSet = new java.util.HashSet<>(Arrays.asList(pIgnoreList));

        Set<Path> dirTree = ignoreSet.isEmpty() ?
                Files.walk(dir).filter(p -> Files.isRegularFile(p)).collect(Collectors.toSet()) :
                Files.walk(dir).filter(p -> Files.isRegularFile(p) && !isPathRelative(p, ignoreSet)).collect(Collectors.toSet());

        return relativize ? relativizePathTree(dir, dirTree) : dirTree;
    }

    private static boolean isPathRelative(Path path, Set<Path> collection) {

        for (Path entry : collection)
        {
            String filename = entry.getFileName().toString();
            boolean isDirectory = FilenameUtils.getExtension(filename).isEmpty();
            boolean matchesFile = !isDirectory && path.getFileName().toString().equals(filename);

            if (matchesFile || path.startsWith(entry))  {
                return true;
            }
        }
        return false;
    }

    public static Path[] joinPaths(Path root, String[] paths) {

        Path[] joined = new Path[paths.length];
        for (int i = 0; i < paths.length; i++) {
            joined[i] = root.resolve(Paths.get(paths[i]));
        }
        return joined;
    }

    private static Set<Path> relativizePathTree(Path root, Set<Path> tree) {

        Set<Path> relativizedTree = new java.util.HashSet<>();
        tree.forEach(path -> relativizedTree.add(root.relativize(path)));
        return relativizedTree;
    }

    public static void validateDirectories(Path dir, Path...other) {

        final Path[] paths = ArrayUtils.add(other, dir);
        Set<Path> directories = new java.util.HashSet<>(Arrays.asList(paths));
        Path[] invalidDirectories = directories.stream()
                .filter(d -> !Files.isDirectory(d)).distinct().toArray(Path[]::new);

        if (invalidDirectories.length > 0)
        {
            Exception e = new NotDirectoryException(Arrays.toString(invalidDirectories));
            throw new IllegalStateException("One or more files are not directories.", e);
        }
    }

    public static java.io.File[] getTextFilesInDirectory(Path dir) {
        return dir.toFile().listFiles(f -> FilenameUtils.getExtension(f.getName()).equals("txt"));
    }

    public static void trimTrailingSpaceFromTextFile(java.io.File file) throws IOException {

        java.util.List<String> newLines = new java.util.ArrayList<>();
        java.util.List<String> lines = org.apache.commons.io.FileUtils.readLines(file, Charset.defaultCharset());
        lines.forEach(l -> newLines.add(l.replaceAll("[\\t\\s]+$", "")));
        org.apache.commons.io.FileUtils.writeLines(file, newLines);
    }
}
