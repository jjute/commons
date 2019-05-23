package commons;

import io.yooksi.commons.util.ReflectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static commons.TestUtils.*;


public class ReflectionTest {

    private class TestData { int id = 0; }

    private static Object target = new ReflectionTest();

    public TestData publicField = new TestData();
    private TestData privateField = new TestData();

    @Test
    public void testReadFieldReflection() throws NoSuchFieldException {

        TestData result = ReflectionUtils.readField(target, "publicField", false, TestData.class);
        Assertions.assertEquals(0, result.id);

        assertIllegalExceptionThrowCause(this::testReadPrivateField, IllegalArgumentException.class);
        assertIllegalExceptionThrowCause(this::testReadFieldClassCastException, ClassCastException.class);
    }

    private void testReadPrivateField() throws NoSuchFieldException {
        ReflectionUtils.readField(target, "privateField", false, TestData.class);
    }

    private void testReadFieldClassCastException() {
        ReflectionUtils.readField(target, "publicField", false, ReflectionTest.class);
    }
}
