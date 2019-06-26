package io.yooksi.commons.util;

import io.yooksi.commons.define.LibraryAnnotation;
import io.yooksi.commons.define.MethodsNotNull;
import org.jetbrains.annotations.Contract;

import javax.validation.constraints.NotEmpty;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public class AnnotationUtils {

    /**
     * <p>Retrieve the given annotation's attributes as a Map, preserving all attribute types.</p>
     * Same can be achieved by using Spring {@code AnnotationUtils} class methods, but for the sake
     * of simplicity this manual approach will do just fine.
     *
     * @return {@code empty} map if no attributes were found.
     */
    public static java.util.Map<String, Object> getAttributes(Annotation annotation) {

        /* Contrary to what one might expect, the elements of an annotation are not attributes,
         * they are actually methods that return the provided value or a default value.
         *
         * Here we are gonna iterate through the annotations' methods and invoke them to get the values.
         * Use annotationType() to get the annotation's class, the object returned by getClass() is just a proxy.
         */
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            try {
                attributes.put(method.getName(), method.invoke(annotation, (Object[]) null));
            }
            catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Unable to get attributes for annotation " +
                        annotation.getClass().getSimpleName(), e);
            }
        }
        return attributes;
    }

    /**
     * <p>Get annotation's attribute and return value in a pair.</p>
     * Since the user can get the value from method instance himself this is
     * done purely for convenience purposes.
     *
     * @param annotation instance of the annotation to get the method from
     * @param attribute complete name <i>(without brackets)</i> of the method to search for
     * @return {@code null} if we were unable to resolve the method
     * @throws NoSuchMethodException if the annotation doesn't have any attribute
     * that corresponds to given parameters
     */
    public static javafx.util.Pair<Method, Object> getAttribute(Annotation annotation, @NotEmpty String attribute) throws NoSuchMethodException {

        try {
            Method object = annotation.annotationType().getDeclaredMethod(attribute);
            return new javafx.util.Pair<>(object, object.invoke(annotation, (Object[]) null));
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to get method for annotation " + annotation.getClass().getSimpleName(), e);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> T getAttributeValue(Annotation annotation, String attribute, Class<T> clazz) {

        Object value;
        try {
            value = getAttribute(annotation, attribute).getValue();
            if (!clazz.isInstance(value)) {
                throw new IllegalArgumentException();
            }
            else return (T) value;
        }
        catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new IllegalStateException(String.format("%s: Expected to find attribute %s(%s) in annotation %s",
                    e.getClass().getName(), attribute, clazz.getSimpleName(), annotation.getClass().getSimpleName()));
        }
    }

    /**
     * @return {@code true} if the supplied annotation belongs to this library.
     * @see LibraryAnnotation
     */
    public static boolean isLibraryAnnotation(Annotation annotation) {
        return annotation.annotationType().getDeclaredAnnotation(LibraryAnnotation.class) != null;
    }

    /**
     * @return {@code true} only if the IntelliJ Contract annotation instance
     * passed as a method parameter guarantees operation immutability <i>(pure = true)</i>
     */
    @Contract(pure = true)
    public static boolean isMethodContractPure(Contract contract) {
        return getAttributeValue(contract, "pure", boolean.class);
    }

    /**
     * Changes the annotation value for the given key of the given annotation
     * to newValue and return the previous value.
     *
     * @author Balder@stackoverflow
     * @see <a href="https://stackoverflow.com/a/28118436/7803527">Origin code on Stackoverflow</a>
     */
    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        java.util.Map<String, Object> memberValues;
        try {
            memberValues = (java.util.Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }
}
