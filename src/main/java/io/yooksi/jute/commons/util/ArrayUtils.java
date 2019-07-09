package io.yooksi.jute.commons.util;

import io.yooksi.jute.commons.define.MethodsNotNull;

import javax.validation.constraints.Positive;
import java.lang.reflect.Array;

@MethodsNotNull
@SuppressWarnings({"unused", "WeakerAccess"})
public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @param <T> array component type
     * @return a new <i>processed</i> copy of the given array
     */
    public static <T> T[] prepend(T element, T[] array) {

        T[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static byte[] prepend(byte element, byte[] array) {

        byte[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static short[] prepend(short element, short[] array) {

        short[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static int[] prepend(int element, int[] array) {

        int[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static long[] prepend(long element, long[] array) {

        long[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static float[] prepend(float element, float[] array) {

        float[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static double[] prepend(double element, double[] array) {

        double[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static boolean[] prepend(boolean element, boolean[] array) {

        boolean[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Expand the array forward for one slot and insert the given element
     * to the beginning of the array so that the corresponding index of the
     * element inside that array is 0.
     *
     * @param element object to prepend
     * @param array the array to process
     * @return a new <i>processed</i> copy of the given array
     */
    public static char[] prepend(char element, char[] array) {

        char[] result = expandArray(array, 1, 0);
        result[0] = element; return result;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     * The index range is determined by second and third method parameters to start from
     * {@code back} and end at {@code array.length - front}. The simplest way to visualize
     * this is to imagine the array expanding on both sides for the already mentioned values.
     *
     * @param array the array to expand
     * @param back amount of slots to expand to the left <i>(has to be positive value)</i>
     * @param front amount of slots to expand to the right  <i>(has to be positive value)</i>
     * @param <T> array component type
     * @return a new <i>expanded</i> copy of the given array
     *
     * @throws IndexOutOfBoundsException if copying would cause access of data outside array bounds.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] expandArray(T[] array, @Positive int back, @Positive int front) {

        T[] expanded = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }


    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static byte[] expandArray(byte[] array, @Positive int back, @Positive int front) {

        byte[] expanded = (byte[]) Array.newInstance(Byte.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static short[] expandArray(short[] array, @Positive int back, @Positive int front) {

        short[] expanded = (short[]) Array.newInstance(Short.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static int[] expandArray(int[] array, @Positive int back, @Positive int front) {

        int[] expanded = (int[]) Array.newInstance(Integer.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static long[] expandArray(long[] array, @Positive int back, @Positive int front) {

        long[] expanded = (long[]) Array.newInstance(Long.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static float[] expandArray(float[] array, @Positive int back, @Positive int front) {

        float[] expanded = (float[]) Array.newInstance(Float.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static double[] expandArray(double[] array, @Positive int back, @Positive int front) {

        double[] expanded = (double[]) Array.newInstance(Double.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static boolean[] expandArray(boolean[] array, @Positive int back, @Positive int front) {

        boolean[] expanded = (boolean[]) Array.newInstance(Boolean.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }

    /**
     * Copies the content of a given array into an index range of a new array of the
     * same type so that the copied array becomes a sub-array of the newly created array.
     *
     * @see #expandArray(Object[], int, int)
     */
    public static char[] expandArray(char[] array, @Positive int back, @Positive int front) {

        char[] expanded = (char[]) Array.newInstance(Character.TYPE, array.length + back + front);
        System.arraycopy(array, 0, expanded, back, array.length);
        return expanded;
    }
}
