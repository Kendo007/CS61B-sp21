package deque;

public class ArrayDeque<T> {
    private int size = 0;
    private T[] items;

    public ArrayDeque() {
        items = (T[]) new Object[100];
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    public void addFirst(T i) {
        System.arraycopy(items, 0, items, 1, size);
        items[0] = i;
        ++size;
    }

    public void addLast(T i) {
        items[size] = i;
        ++size;
    }

    public T removeFirst() {
        if (isEmpty()) { return null; }

        T temp = items[0];
        System.arraycopy(items, 1, items, 0, size - 1);
        --size;

        return temp;
    }

    public T removeLast() {
        if (isEmpty()) { return null; }

        T temp = items[size - 1];
        items[size - 1] = null;
        --size;

        return temp;
    }

    public T get(int index) {
        if (index >= size || index < 0) { return null; }

        return items[index];
    }

    public void printDeque() {
        for (int i = 0; i < size; ++i) {
            System.out.print(items[i] + " ");
        }

        System.out.println();
    }
}
