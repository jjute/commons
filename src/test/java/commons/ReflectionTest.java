package commons;

import io.yooksi.commons.util.ReflectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static commons.TestUtils.*;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ReflectionTest {

    private class TestData {}
    private static final Object target = new ReflectionTest();

    public TestData publicField = new TestData();
    private TestData privateField = new TestData();

    @Test
    public void testReadFieldReflection() {

        TestData result = ReflectionUtils.readField(target, "publicField", false, TestData.class);
        Assertions.assertNotNull(result);

        assertIllegalExceptionThrowCause(this::testReadPrivateField, IllegalArgumentException.class);
        assertIllegalExceptionThrowCause(this::testReadFieldClassCastException, ClassCastException.class);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void testReadPrivateField() {
        ReflectionUtils.readField(target, "privateField", false, TestData.class);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void testReadFieldClassCastException() {
        ReflectionUtils.readField(target, "publicField", false, ReflectionTest.class);
    }
}
