package deque;

import java.util.Comparator;

public class MyComparators {
    public static Comparator<String> getStringMax() {
        return new MaxString();
    }

    public static Comparator<Integer> getIntegerMax() {
        return new MaxInteger();
    }

    private static class MaxString implements Comparator<String> {

        @Override
        public int compare(String t, String t1) {
            return t.length() - t1.length();
        }
    }

    private static class MaxInteger implements Comparator<Integer> {

        @Override
        public int compare(Integer integer, Integer t1) {
            return integer.compareTo(t1);
        }
    }
}
