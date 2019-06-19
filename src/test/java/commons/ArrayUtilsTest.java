package commons;

import io.yooksi.commons.util.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayUtilsTest {

    @Test
    public void expandArrayForwardTest() {
        expandArrayTest(0, 4, -1, 3);
    }

    @Test
    public void expandArrayBackwardsTest() {
        expandArrayTest(2, 0, 1, -1);
    }

    @Test
    public void expandArrayFrontAndBackTest() {
        expandArrayTest( 6, 11, 5, 10);
    }

    private void expandArrayTest(int back, int front, Integer nullIndexBack, Integer nullIndexFront) {

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
