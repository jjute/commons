package commons;

import io.yooksi.commons.util.FileUtils;
import javafx.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class FileUtilsTest {

    private static final Path ROOT_PATH = Paths.get(System.getProperty("user.dir"));
    private static final java.util.Random RAND = new java.util.Random();

    @Test
    public void getDirectoryTreeTest() throws IOException {

        Path path = ROOT_PATH.resolve("testDir");
        int filesCreated = constructTestDirTree(path);

        Set<Path> result = FileUtils.getDirectoryTree(path, false);
        Assertions.assertEquals(filesCreated, result.size());

        System.out.printf("Created %d files for this test%n", result.size());
        result.forEach(p -> System.out.printf("Path: %s%n", p.toString()));
    }

    @Test
    public void getDirectoryTreeExcludingSomeTest() throws  IOException {

        int excludeFiles = RAND.nextInt(3) + 1;
        final String[] excludeList = new String[excludeFiles];
        Set<String> excludeSet = new java.util.HashSet<>();

        for (int i = 0; i < excludeFiles; i++) {
            excludeList[i] = String.valueOf(i) + ".txt";
            excludeSet.add(excludeList[i]);
        }
        Path path = ROOT_PATH.resolve("testDir");
        int filesExcluded = constructTestDirTree(path, excludeSet);

        Set<Path> createdFiles = FileUtils.getDirectoryTree(path, false);
        Set<Path> filteredFiles = FileUtils.getDirectoryTree(path, false, excludeList);
        Assertions.assertEquals(createdFiles.size() - filesExcluded, filteredFiles.size());

        System.out.printf("Created %d and excluded %d files for this test%n", createdFiles.size(), filesExcluded);
        createdFiles.forEach(p -> System.out.printf("Path: %s%s%n", p.toString(),
                excludeSet.contains(p.getFileName().toString()) ? " -- EXCLUDED" : ""));
    }

    /**
     * Construct a directory tree with a random number of recursive sub-directories, each
     * containing a random number of empty text files. Filenames given to both sub-directories
     * and text files are determined by the order in which they were generated.
     * Random number value bounds are hard-coded and inclusive.
     *
     * @param path root {@code Path} to start constructing from
     * @param excludedFiles list of filenames that represent excluded files
     * @return a {@code Pair} who's left side and right side represent the number of
     *         files <i>(not directories)</i> created and a count of how many of those
     *         created files are considered excluded.
     *
     * @throws IOException when something went wrong when creating or deleting files or directories.
     */
    private Pair<Integer, Integer> constructDirTree(Path path, Set<String> excludedFiles) throws IOException {

        java.io.File rootDir = path.toFile();
        if (rootDir.exists()) {
            // Delete everything from this directory
            org.apache.commons.io.FileUtils.cleanDirectory(rootDir);
        }
        else Assertions.assertTrue(path.toFile().mkdir());

        int levels = RAND.nextInt(5) + 1;
        int created = 0, excluded = 0;
        /*
         * Create a random number of recursive sub-directories
         */
        for (int i1 = 0; i1 < levels; i1++)
        {
            path = path.resolve(String.valueOf(i1));
            Assertions.assertTrue(path.toFile().mkdir());
            /*
             * Create a random number of empty text files
             * within the current directory under path variable
             */
            for (int i2 = RAND.nextInt(10) + 1; i2 > 0; i2--, created++)
            {
                String filename = String.valueOf(i2) + ".txt";
                if (excludedFiles.contains(filename)) {
                    excluded++;
                }
                java.io.File file = new java.io.File(path.resolve(filename).toString());
                Assertions.assertTrue(file.createNewFile());
            }
        }
        return new Pair<>(created, excluded);
    }
    private int constructTestDirTree(Path path) throws IOException {
        return constructDirTree(path, new java.util.HashSet<>()).getKey();
    }
    private int constructTestDirTree(Path path, Set<String> excludedFiles) throws IOException {
        return constructDirTree(path, excludedFiles).getValue();
    }
}
