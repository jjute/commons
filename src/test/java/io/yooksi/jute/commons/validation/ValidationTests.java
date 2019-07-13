package io.yooksi.jute.commons.validation;

import io.yooksi.jute.commons.aop.AOPProxy;
import io.yooksi.jute.commons.define.PositiveRange;
import io.yooksi.jute.commons.logger.LibraryLogger;
import io.yooksi.jute.commons.util.ArrayUtils;
import io.yooksi.jute.commons.validator.BeanValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.groups.Default;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ValidationTests {

    public static class Parent {

        public Parent(@PositiveOrZero int a, @NotEmpty String b) {}
    }
    public static class FirstChild extends Parent {

        public FirstChild(int a, String b) {
            super(a, b);
        }
    }
    public static class SecondChild extends FirstChild {

        public SecondChild(int a, String b) {
            super(a, b);
        }
    }
    public static class ThirdChild extends FirstChild {

        public ThirdChild(int a, String b, ValidationTests c) {
            super(a, b);
        }
    }

    private final Object[] paramsValid = new Object[] { 1, "sample" };
    private final Object[] paramsInvalid = new Object[] { -1, "" };

    @Test
    public void testBasicConstructorValidation() {

        // Construct and validate parent
        Parent parent1 = BeanValidator.constructParent(Parent.class, FirstChild.class, paramsValid);
        assertBeanViolationCount(0);

        Parent parent2 = BeanValidator.constructParent(Parent.class, FirstChild.class, paramsInvalid);
        assertBeanViolationCount(2);

        // Construct and validate the first child
        FirstChild firstChild1 = BeanValidator.constructChild(Parent.class, FirstChild.class, paramsValid);
        assertBeanViolationCount(0);

        FirstChild firstChild2 = BeanValidator.constructChild(Parent.class, FirstChild.class, paramsInvalid);
        assertBeanViolationCount(2);
    }

    @Test
    public void testIntermediaryConstructorValidation() {

        // Construct and validate the second child
        SecondChild secondChild1 = BeanValidator.constructChild(Parent.class, SecondChild.class, paramsValid);
        assertBeanViolationCount(0);

        SecondChild secondChild2 = BeanValidator.constructChild(Parent.class, SecondChild.class, paramsInvalid);
        assertBeanViolationCount(2);
    }

    @Test
    public void testUnequalConstructorValidation() {

        // Construct and validate the third child
        ThirdChild thirdChild1 = BeanValidator.constructChild(Parent.class, ThirdChild.class,
                ArrayUtils.add(paramsValid, new ValidationTests()));

        assertBeanViolationCount(0);

        ThirdChild thirdChild2 = BeanValidator.constructChild(Parent.class, ThirdChild.class,
                ArrayUtils.add(paramsInvalid, new ValidationTests()));

        assertBeanViolationCount(2);
    }

    @Test
    public void testObjectFieldValidation() {

        ValidationTestClass testClass = new ValidationTestClass();

        BeanValidator.validate(testClass, ValidationTestClass.accessibleFieldChecks.class);
        ValidationTests.assertBeanViolationCount(2);

        BeanValidator.validate(testClass);
        ValidationTests.assertBeanViolationCount(3);

        BeanValidator.validate(testClass, ValidationTestClass.accessibleFieldChecks.class, Default.class);
        ValidationTests.assertBeanViolationCount(5);
    }

    @Test
    public void testMethodParameterValidation() {

        ValidationTests test = AOPProxy.createValidationProxy(new ValidationTests());
        test.callMethodParameterValidation(null, 20);
        ValidationTests.assertBeanViolationCount(2);
    }

    @Test
    @SuppressWarnings("unused")
    public void testMethodReturnValueValidation() {

        ValidationTests test = AOPProxy.createValidationProxy(new ValidationTests());
        Object result = test.callMethodReturnValueValidation();
        ValidationTests.assertBeanViolationCount(1);
    }

    public void callMethodParameterValidation(@NotNull Object arg1, @PositiveRange(max=10) int arg2) {
        LibraryLogger.info("Method should have been intercepted with args [%s, %d]", arg1, arg2);
    }

    @SuppressWarnings("SameReturnValue")
    public @NotNull Object callMethodReturnValueValidation() {
        return null;
    }

    private static void assertBeanViolationCount(int expectation) {

        Assertions.assertEquals(expectation, BeanValidator.recentViolations.size());
        BeanValidator.recentViolations.clear();
    }
}
