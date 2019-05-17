package commons;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestUtils {

    public static void assertTextFileLineCount(java.io.File file, int count) throws IOException {
        assertEquals("Expected " + count + " console outputs", count, getTextFileLineCount(file));
    }

    public static long getTextFileLineCount(java.io.File file) throws IOException {
        return java.nio.file.Files.lines(file.toPath()).count();
    }
}
