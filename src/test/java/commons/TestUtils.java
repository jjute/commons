package commons;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("WeakerAccess")
class TestUtils {

    static void assertTextFileLineCount(java.io.File file, int count) throws IOException {

        long lineCount = getTextFileLineCount(file);
        assertEquals("Expected " + count + " console outputs", count, lineCount);
    }

    static long getTextFileLineCount(java.io.File file) throws IOException {
        return java.nio.file.Files.lines(file.toPath()).count();
    }
}
