package io.yooksi.jute.commons.tool;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

@SuppressWarnings("WeakerAccess")
public class TestUtils {

    public static void assertThrowCause(Throwable t, Class<? extends Throwable> c) {
        Assertions.assertEquals(c, t.getCause().getClass());
    }

    public static IllegalStateException assertThrowIllegalException(Executable executable) {
        return Assertions.assertThrows(IllegalStateException.class, executable);
    }

    public static void assertIllegalExceptionThrowCause(Executable executable, Class<? extends Throwable> cause) {
        assertThrowCause(assertThrowIllegalException(executable), cause);
    }
}
