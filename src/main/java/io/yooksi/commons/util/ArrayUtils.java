package io.yooksi.commons.util;

import io.yooksi.commons.define.MethodsNotNull;

import javax.validation.constraints.Positive;
import java.lang.reflect.Array;

@MethodsNotNull
@SuppressWarnings("unused")
public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {

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
