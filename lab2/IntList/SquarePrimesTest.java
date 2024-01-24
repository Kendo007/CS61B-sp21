package IntList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class SquarePrimesTest {

    /**
     * Here is a test for isPrime method. Try running it.
     * It passes, but the starter code implementation of isPrime
     * is broken. Write your own JUnit Test to try to uncover the bug!
     */
    @Test
    public void testSquarePrimesSingle() {
        IntList lst = IntList.of(14, 15, 16, 17, 18);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 18", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesMultiple() {
        IntList lst = IntList.of(14, 15, 16, 17, 23);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("14 -> 15 -> 16 -> 289 -> 529", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testSquarePrimesFirstLast() {
        IntList lst = IntList.of(3, 15, 16, 17, 23);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("9 -> 15 -> 16 -> 289 -> 529", lst.toString());
        assertTrue(changed);
    }

    @Test
    public void testNoPrimes() {
        IntList lst = IntList.of(4, 15, 16, 18, 20);
        boolean changed = IntListExercises.squarePrimes(lst);
        assertEquals("4 -> 15 -> 16 -> 18 -> 20", lst.toString());
        assertFalse(changed);
    }
}
