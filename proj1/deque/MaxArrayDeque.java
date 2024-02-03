package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> c;
    public MaxArrayDeque(Comparator<T> c) {
        this.c = c;
    }

    public T max() {
        return max(this.c);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        T max = get(0);

        for (T i : this) {
            if (c.compare(i, max) > 0) {
                max = i;
            }
        }

        return max;
    }
}
