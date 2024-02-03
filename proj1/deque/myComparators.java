package deque;

import java.util.Comparator;

public class myComparators {
    public static Comparator<String> getStringMax() {
        return new maxString();
    }

    public static Comparator<Integer> getIntegerMax() {
        return new maxInteger();
    }

    private static class maxString implements Comparator<String> {

        @Override
        public int compare(String t, String t1) {
            return t.length() - t1.length();
        }
    }

    private static class maxInteger implements Comparator<Integer> {

        @Override
        public int compare(Integer integer, Integer t1) {
            return integer.compareTo(t1);
        }
    }
}
