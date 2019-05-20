package commons;

import io.yooksi.commons.aop.AOPProxy;
import io.yooksi.commons.define.PositiveRange;
import io.yooksi.commons.logger.LibraryLogger;
import io.yooksi.commons.validator.BeanValidator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import static org.junit.Assert.*;

@SuppressWarnings("WeakerAccess")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ValidationTest {

    @Test
    public void testObjectFieldValidation() {

        TestClass testClass = new TestClass();

        BeanValidator.validate(testClass,TestClass.accessibleFieldChecks.class);
        ValidationTest.assertBeanViolationCount(2);

        BeanValidator.validate(testClass);
        ValidationTest.assertBeanViolationCount(3);

        BeanValidator.validate(testClass, TestClass.accessibleFieldChecks.class, Default.class);
        ValidationTest.assertBeanViolationCount(5);
    }

    @Test
    public void testMethodParameterValidation() {

        ValidationTest test = AOPProxy.createValidationProxy(new ValidationTest());
        test.callMethodParameterValidation(null, 20);
        ValidationTest.assertBeanViolationCount(2);
    }

    @Test
    public void testMethodReturnValueValidation() {

        ValidationTest test = AOPProxy.createValidationProxy(new ValidationTest());
        Object result = test.callMethodReturnValueValidation();
        ValidationTest.assertBeanViolationCount(1);
    }

    public void callMethodParameterValidation(@NotNull Object arg1, @PositiveRange(max=10) int arg2) {
        LibraryLogger.info("Method should have been intercepted with args [%s, %d]", arg1, arg2);
    }

    public @NotNull Object callMethodReturnValueValidation() {
        return null;
    }

    private static void assertBeanViolationCount(int expectation) {

        String log = "Expected to fail " + expectation + " validations";
        assertEquals(log, expectation, BeanValidator.recentViolations.size());
        BeanValidator.recentViolations.clear();
    }
}
