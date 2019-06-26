package commons;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

@SuppressWarnings("WeakerAccess")
class TestUtils {

    static void assertTextFileLineCount(java.io.File file, int count) throws IOException {

        long lineCount = getTextFileLineCount(file);
        Assertions.assertEquals(count, lineCount);
    }

    static long getTextFileLineCount(java.io.File file) throws IOException {
        return java.nio.file.Files.lines(file.toPath()).count();
    }
}
