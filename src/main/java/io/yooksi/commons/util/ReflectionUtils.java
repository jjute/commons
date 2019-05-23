package io.yooksi.commons.util;

import io.yooksi.commons.define.MethodsNotNull;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

@MethodsNotNull
public class ReflectionUtils {

    /**
     * Read the named {@link Field} in target object and return
     * the value cast to {@code clazz} parameter using reflection.
     *
     * @param target the object to reflect, must not be {@code null}
     * @param name the field name to obtain
     * @param clazz used to cast the return value
     * @return the field value
     *
     * @throws IllegalStateException when the {@code clazz} parameter
     * does not match the target field class, field with the specified
     * name is not found or the named field is not made accessible.
     *
     * @deprecated this method offers a pure Java solution but lacks some
     * of the more advanced handling Apache Commons offers. It is recommended
     * to use {@link #readField(Object, String, boolean, Class)} instead.
     */
    @Deprecated
    @Contract(pure = true)
    @SuppressWarnings("ConstantConditions")
    public static <T> T getPrivateFieldValue(Object target, String name, Class<T> clazz) {

        Object value = null;
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);

            value = field.get(target);
            return clazz.cast(value);
        }
        catch (ClassCastException e)
        {
            String log = "Parameter %s does not match target field class %s";
            throw new IllegalStateException(String.format(log, clazz, value.getClass()), e);
        }
        catch (NoSuchFieldException e)
        {
            String log = "Unable to find field \"name\" in " + target.getClass().getSimpleName();
            throw new IllegalStateException(log, e);
        }
        catch (IllegalAccessException e)
        {
            String log = "Unable to access field \"%s\" in object %s";
            throw new IllegalStateException(String.format(log, name, target), e);
        }
    }

    /**
     * Use Apache Commons Language library to read the named {@link Field} in target
     * object and return the value cast to {@code clazz} parameter using reflection.
     *
     * @param target the object to reflect, must not be {@code null}
     * @param fieldName the field name to obtain
     * @param forceAccess whether to break scope restrictions using the
     *      {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} method.
     *      {@code false} will only match {@code public} fields.
     * @param clazz used to cast the return value
     * @return the field value
     *
     * @throws IllegalStateException if the field is not found or is not accessible,
     * the return value cannot be cast to class parameter or a security related error occurred.
     *
     * @see FieldUtils#readField(Field, Object, boolean)
     */
    @Contract(pure = true)
    @SuppressWarnings("ConstantConditions")
    public static <T> @Nullable T readField(Object target, String fieldName, boolean forceAccess, Class<T> clazz) {

        Object value = null;
        try {
             value = FieldUtils.readField(target, fieldName, forceAccess);
             return clazz.cast(value);
        }
        catch (ClassCastException e)
        {
            String log = "Parameter %s does not match target field class %s";
            throw new IllegalStateException(String.format(log, clazz, value.getClass()), e);
        }
        catch (IllegalAccessException | SecurityException e)
        {
            String log = "Unable to access field \"%s\" in object %s";
            throw new IllegalStateException(String.format(log, fieldName, target), e);
        }
        catch (IllegalArgumentException e)
        {
            String log = "Field %s could not be found because " + (forceAccess ? "it doesn't exist" :
                    "access is private and method was instructed not to force access");

            throw new IllegalStateException(String.format(log, fieldName), e);
        }
    }

    /**
     * Helper method to read object fields marked as private or protected using reflection.
     * @see #readField(Object, String, boolean, Class)
     */
    public static <T> @Nullable T readPrivateField(Object target, String fieldName, Class<T> clazz) {
        return readField(target, fieldName, true, clazz);
    }
}
