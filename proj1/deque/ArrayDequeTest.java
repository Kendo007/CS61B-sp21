package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create ArrayDeques with different parameterized types*/
    public void multipleParamTest() {
        ArrayDeque<String>  lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    public void getDataTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 5001; i++) {
            lld1.addLast(i);
        }

        assertEquals(100, (long) lld1.get(100));
        assertEquals(2500, (long) lld1.get(2500));
        assertEquals(2501, (long) lld1.get(2501));
        assertEquals(5000, (long) lld1.get(5000));
        assertNull(lld1.get(10000));
    }

    @Test
    public void randomizedTest() {
        LinkedListDeque<Integer> L = new LinkedListDeque<>();
        ArrayDeque<Integer> B = new ArrayDeque<>();

        int N = 50000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 7);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100000);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                int randVal = StdRandom.uniform(0, 100000);
                L.addFirst(randVal);
                B.addFirst(randVal);
            } else if (operationNumber == 2 && !L.isEmpty()) {
                assertEquals(L.removeFirst(), B.removeFirst());
            } else if (operationNumber == 3 && !L.isEmpty()) {
                assertEquals(L.removeLast(), B.removeLast());
            } else if (operationNumber == 4) {
                assertEquals(L.size(), B.size());
            } else if (operationNumber == 5 && !L.isEmpty()) {
                int randVal = StdRandom.uniform(0, L.size());
                Integer val = L.get(randVal);
                assertEquals(val, B.get(randVal));
                assertEquals(val, L.getRecursive(randVal));
            } else if (operationNumber == 6) {
                assertEquals(L, B);
            }
        }

        System.out.println();
    }

    @Test
    public void testingEquals() {
        ArrayDeque<Integer> A = new ArrayDeque<>();
        ArrayDeque<Integer> B = new ArrayDeque<>();

        int N = 50000;
        for (int i = 0; i < N; ++i) {
            int randVal = StdRandom.uniform(0, 10000);
            A.addLast(randVal);
            B.addLast(randVal);
        }

        assertEquals(A, B);

        A.removeFirst();
        A.addFirst(52);

        assertNotEquals(A, B);
    }

    @Test
    public void loopTest() {
        ArrayDeque<Integer> A = new ArrayDeque<>();
        LinkedListDeque<Integer> B = new LinkedListDeque<>();

        int N = 500;
        for (int i = 0; i < N; ++i) {
            int randVal = StdRandom.uniform(0, 10000);
            int x = StdRandom.uniform(0, 2);

            switch (x) {
                case 0:
                    A.addLast(randVal);
                    B.addLast(randVal);
                    break;
                case 1:
                    A.addFirst(randVal);
                    B.addFirst(randVal);
                    break;
            }
        }

        for (Integer i : A) {
            System.out.print(i + " ");
        }

        System.out.println();

        for (Integer i : B) {
            System.out.print(i + " ");
        }
    }

    @Test
    public void testMax() {
        MaxArrayDeque<String> A = new MaxArrayDeque<>(MyComparators.getStringMax());
        A.addLast("hi");
        A.addLast("eggs");
        A.addLast("pyjamas");

        System.out.println(A.max());

        MaxArrayDeque<Integer> B = new MaxArrayDeque<>(MyComparators.getIntegerMax());
        B.addFirst(87);
        B.addFirst(900);
        B.addLast(780);

        System.out.println(B.max());
    }
}
