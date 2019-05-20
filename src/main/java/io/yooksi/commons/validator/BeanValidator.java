package io.yooksi.commons.validator;

import io.yooksi.commons.define.MethodsNotNull;

import io.yooksi.commons.logger.LibraryLogger;
import io.yooksi.commons.util.AnnotationUtils;
import io.yooksi.commons.util.StringUtils;

import javafx.util.Pair;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Contract;

import javax.validation.groups.Default;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MethodsNotNull
public final class BeanValidator {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final ExecutableValidator exeValidator = factory.getValidator().forExecutables();
    private static final Validator validator = factory.getValidator();

    private static final char REGEX_KEY = '$';

    /**
     * This pair is used to store regex data used to parse annotation violation messages.
     * <ul>
     *     <li>Key - A compiled representation of the parsing regex.</li>
     *     <li>Value - Regex group that contains the value we want to capture.</li>
     * </ul>
     */
    private static final javafx.util.Pair<Pattern, Integer> PARSE_REGEX = new Pair<>(
            Pattern.compile(String.format("(?:\\%s)([a-zA-Z0-9_-]*)", REGEX_KEY)), 1);

    /* Make the constructor private to disable instantiation */
    private BeanValidator() {
        throw new UnsupportedOperationException();
    }

    /**
     * Validate and process all constraints on object.
     *
     * @param groups the group or list of groups targeted for validation (defaults to {@link Default})
     * @param object instance of the object to validate
     * @param <T> object type being validated
     * @return object being validated <i>(for convenience)</i>
     * @throws javax.validation.UnexpectedTypeException No validator could be found for constraint
     * given to object being validated. This happens when the object data type doesn't match the
     * data type the validator was intended to validate.
     * @see #processViolation(ConstraintViolation)
     */
    public static <T> T validate(T object, Class<?>...groups) {

        LibraryLogger.debug("Validating object %s", object);
        for (ConstraintViolation violation : validator.validate(object, groups)) {
            processViolation(violation);
        }
        return object;
    }

    /**
     * <p>Validate method parameters with Java Bean validation.</p>
     * Usually called from a method interception system.
     *
     * @param method the method for which the parameter constraints is validated
     * @param object the object on which the method to validate is invoked
     * @param params  the values provided by the caller for the given method's parameters
     * @param <T> the type hosting the method to validate
     */
    @Contract(pure = true)
    public static <T> void validateMethod(Method method, T object, Object...params) {

        for (ConstraintViolation violation : exeValidator.validateParameters(object, method, params)) {
            processViolation(violation);
        }
    }

    /**
     * Called from a method interception system to validate method parameters,
     * return value and the state of the class instance after it's invocation.
     *
     * @param mi method joinpoint given to an interceptor upon method-call
     * @return method invocation return value
     * @throws Throwable if the joinpoint throws an exception
     */
    public static Object validateMethod(MethodInvocation mi) throws Throwable {

        validateMethod(mi.getMethod(), mi.getThis(), mi.getArguments());
        /*
         * After the method parameters have been validated let the method
         * execute it's operations and get the return value
         */
        Object result = mi.proceed();
        /*
         * Now validate the class instance that holds the method to see if
         * all fields are still complying with annotation constraints.
         * However do this only if the method is not explicitly annotated
         * with a contract that gurantees operation immutability.
         */
        Contract contract = mi.getMethod().getDeclaredAnnotation(Contract.class);
        if (contract == null || !AnnotationUtils.isMethodContractPure(contract)) {
            validate(mi.getThis());
        }
        // TODO: Validate return value here as well
        return result;
    }

    /**
     * <p>Create, initialize and <b>validate</b> a new instance of a child class.</p>
     * A child class is only recognized as valid if it extends it's parent base class.
     *
     * @param params constructor initialization parameters
     * @return newly constructed and validated object instance
     * @throws IllegalArgumentException when no child or parent constructor
     * with the supplied parameters could be found
     */
    @SuppressWarnings("unchecked")
    public static <T> T constructChild(Class<? super T> parentClass, Class<T> childClass, Object...params) {

        /* Bean constraint validation doesn't seem to process parent constructors
         * so we have to manually validate their parameters first
         */
        Constructor<T> constructor = getConstructor(parentClass, params);
        java.util.Set<ConstraintViolation<T>> parentViolations = validateConstructorParams(constructor, params);

        constructor = getConstructor(childClass, params);
        java.util.Set<ConstraintViolation<T>> childViolations = validateConstructorParams(constructor, params);

        /* In case both child and parent constructor produced constraint violations
         * on the same method parameters we need to filter the child constructor
         * violations to exclude the duplicates so we don't do double prints.
         */
        java.util.Set<Object> violationValues = new java.util.HashSet<>();
        parentViolations.forEach(v -> violationValues.add(v.getInvalidValue()));
        for (ConstraintViolation v : childViolations) {
            if (!violationValues.contains(v.getInvalidValue()))
                parentViolations.add(v);
        }
        /* Both parent and child violations will be processed here
         */
        for (ConstraintViolation violation : parentViolations) {
            processViolation(violation);
        }
        return construct(constructor, params);
    }

    /**
     * Find the constructor object for a declared class constructor with the specified parameter list.
     * The order of parameters in the argument array has to match the constructor parameter order.
     *
     * @param clazz Class to get the declared constructor from
     * @param params list of constructor parameters
     */
    public static <T> Constructor getConstructor(Class<T> clazz, Object...params) {

        Class[] paramClasses = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramClasses[i] = params[i].getClass();
        }
        /* Use Apache method of finding an accessible constructor as it
         * is a more flexible search than the normal exact matching algorithm.
         */
        Constructor c = ConstructorUtils.getMatchingAccessibleConstructor(clazz, paramClasses);
        if (c == null) {
            String sParams = java.util.Arrays.toString(params);
            String log = String.format("Unable to find constructor for class %s with parameters %s", clazz, sParams);
            throw new IllegalArgumentException(new NoSuchMethodException(log));
        }
        else return c;
    }

    /**
     * This is a helper method to create, initialize and <b>validate</b> a new instance of the
     * constructor's declaring class, with the specified initialization parameters.
     *
     * @param params initialization parameters
     * @return newly constructed and validated object
     */
    private static <T> T construct(Constructor<T> constructor, Object...params) {

        try {
            return validate(constructor.newInstance(params));
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <p>This method gets called whenever new violations are returned by validators.</p>
     * It will parse the constraint message and print it with the appropriate level.
     */
    private static void processViolation(ConstraintViolation violation) {

        Object value = violation.getInvalidValue();
        Object field = violation.getPropertyPath();
        String message = violation.getMessage();
        Level level = Level.ERROR;

        Annotation annotation = violation.getConstraintDescriptor().getAnnotation();
        /*
         * Process violation message and level only if the
         * annotation belongs to commons library.
         */
        if (AnnotationUtils.isLibraryAnnotation(annotation))
        {
            java.util.Map<String, Object> attributes = AnnotationUtils.getAttributes(annotation);
            String sLevel = AnnotationUtils.getAttributeValue(annotation, "level", String.class);
            level = Level.toLevel(sLevel, level);

            /* Parse our annotation violation message and replace all words marked with the regex key
             * with an annotation attribute value that holds the same name.
             */
            Matcher matcher = PARSE_REGEX.getKey().matcher(message);
            while (matcher.find()) {
                String group = matcher.group(PARSE_REGEX.getValue());
                Object oReplacement = group.equals("value") ? value : attributes.get(group);

                String sReplacement = StringUtils.smartQuote(oReplacement);
                message = message.replace(REGEX_KEY + group, sReplacement);
            }
        }

        /* Print the violation message to console with the appropriate level.
         * Also print an exception stack trace as a debug log
         */
        LibraryLogger.printf(level, message);
        LibraryLogger.debug(message, new Exception(String.format("Field '%s' with value '%s' has violated " +
                "annotation constrains of %s", field, value, annotation.annotationType().getSimpleName())));
    }

    private static <T> java.util.Set<ConstraintViolation<T>> validateConstructorParams(Constructor<T> c, Object...p) {
        return exeValidator.validateConstructorParameters(c, p);
    }
}
