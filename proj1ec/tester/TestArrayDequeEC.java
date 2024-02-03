package tester;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

import static org.junit.Assert.assertEquals;

public class TestArrayDequeEC {
    @Test
    public void randomizedTest() {
        Integer expected, actual;
        StringBuilder errorMsg = new StringBuilder();

        ArrayDequeSolution<Integer> L = new ArrayDequeSolution<>();
        StudentArrayDeque<Integer> B = new StudentArrayDeque<>();

        int N = 500;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);

            if (operationNumber == 0) {
                int randVal = StdRandom.uniform(0, 10000);
                errorMsg.append("addFirst(").append(randVal).append(")\n");

                L.addFirst(randVal);
                B.addFirst(randVal);
            } else if (operationNumber == 1) {
                int randVal = StdRandom.uniform(0, 10000);
                errorMsg.append("addLast(").append(randVal).append(")\n");

                L.addLast(randVal);
                B.addLast(randVal);
            } else if (operationNumber == 2 && !B.isEmpty()) {
                expected = L.removeFirst();
                actual = B.removeFirst();

                errorMsg.append("removeFirst()\n");
                assertEquals(errorMsg.toString(), expected, actual);
            } else if (operationNumber == 3 && !B.isEmpty()) {
                expected = L.removeLast();
                actual = B.removeLast();

                errorMsg.append("removeLast()\n");
                assertEquals(errorMsg.toString(), expected, actual);
            }
        }
    }
}