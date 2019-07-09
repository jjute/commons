package io.yooksi.jute.commons.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.yooksi.jute.commons.tool.TestUtils.*;

@SuppressWarnings({"unused", "WeakerAccess", "ResultOfMethodCallIgnored"})
public class ReflectionUtilsTests {

    private class TestData {}
    private static final Object target = new ReflectionUtilsTests();

    public TestData publicField = new TestData();
    private TestData privateField = new TestData();

    private class Caller
    {
        private Class getCallerClass() {
            return ReflectionUtils.getCallerClass(1);
        }
        private Class getCurrentCallerClass() {
            return ReflectionUtils.getCallerClass(0);
        }
        private void failGettingCallerClass() {
            ReflectionUtils.getCallerClass(-1);
        }
    }

    @Test
    public void testGetCallerClass() {

        Caller caller = new Caller();

        Assertions.assertDoesNotThrow(caller::getCurrentCallerClass);
        Assertions.assertEquals(Caller.class, caller.getCurrentCallerClass());
        Assertions.assertEquals(ReflectionUtilsTests.class, caller.getCallerClass());

        Assertions.assertThrows(IllegalArgumentException.class, caller::failGettingCallerClass);
    }

    @Test
    public void testReadFieldReflection() {

        TestData result = ReflectionUtils.readField(target, "publicField", false, TestData.class);
        Assertions.assertNotNull(result);

        assertIllegalExceptionThrowCause(this::testReadPrivateField, IllegalArgumentException.class);
        assertIllegalExceptionThrowCause(this::testReadFieldClassCastException, ClassCastException.class);
    }

    private void testReadPrivateField() {
        ReflectionUtils.readField(target, "privateField", false, TestData.class);
    }
    private void testReadFieldClassCastException() {
        ReflectionUtils.readField(target, "publicField", false, ReflectionUtilsTests.class);
    }
}
