package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> aList = new AListNoResizing<>();
        BuggyAList<Integer> buggyList = new BuggyAList<>();

        aList.addLast(4);
        buggyList.addLast(4);
        aList.addLast(5);
        buggyList.addLast(5);
        aList.addLast(6);
        buggyList.addLast(6);

        assertEquals(aList.size(), buggyList.size());

        assertEquals(aList.removeLast(), buggyList.removeLast());
        assertEquals(aList.removeLast(), buggyList.removeLast());
        assertEquals(aList.removeLast(), buggyList.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 1) {
                assertEquals(L.size(), B.size());
            } else if (operationNumber == 2 && L.size() > 0) {
                assertEquals(L.getLast(), B.getLast());
            } else if (operationNumber == 3 && L.size() > 0) {
                assertEquals(L.removeLast(), B.removeLast());
            }
        }
    }
}
