package io.yooksi.jute.commons.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("WeakerAccess")
public class ArrayUtilsTests {

    @Test
    @SuppressWarnings("ConstantConditions")
    public void prependArrayTest() {

        Integer[] refArray = ArrayUtils.prepend(1, new Integer[] { 2, 3 });
        Assertions.assertEquals(1, refArray[0]);

        byte[] primitiveArray = ArrayUtils.prepend((byte) 1, new byte[] { 2, 3 });
        Assertions.assertEquals(1, primitiveArray[0]);

        Boolean[] emptyFirstArray = ArrayUtils.prepend(null, new Boolean[] { true, false });
        Assertions.assertNull(emptyFirstArray[0]);
    }

    @Test
    public void expandArrayForwardTest() {
        expandArrayTest(0, 4);
    }

    @Test
    public void expandArrayBackwardsTest() {
        expandArrayTest(2, 0);
    }

    @Test
    public void expandArrayFrontAndBackTest() {
        expandArrayTest( 6, 11);
    }

    private void expandArrayTest(int back, int front) {

        Integer[] refArray = ArrayUtils.expandArray(new Integer[] { 1, 2, 3 }, back, front);
        Assertions.assertEquals(3 + back + front, refArray.length);
        for (int i = 0; i < back - 1; i++) {
            Assertions.assertNull(refArray[i]);
        }
        if (front > 0) {
            for (int i = refArray.length - 1; i >= front - 1; i--) {
                Assertions.assertNull(refArray[i]);
            }
        } else {
            Assertions.assertNotNull(refArray[refArray.length - 1]);
        }
        int[] primitiveArray = ArrayUtils.expandArray(new int[] { 1, 2, 3 }, back, front);
        Assertions.assertEquals(3 + back + front, primitiveArray.length);
        for (int i = 0; i < back - 1; i++) {
            Assertions.assertEquals(0, primitiveArray[i]);
        }
        if (front > 0) {
            for (int i = refArray.length - 1; i >= front - 1; i--) {
                Assertions.assertEquals(0, primitiveArray[i]);
            }
        } else {
            Assertions.assertNotEquals(0, refArray[refArray.length - 1]);
        }
        Integer[] emptyArray = ArrayUtils.expandArray(new Integer[]{}, back, front);
        Assertions.assertEquals(back + front, emptyArray.length);
    }
}
