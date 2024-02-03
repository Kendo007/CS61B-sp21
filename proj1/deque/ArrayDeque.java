package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size = 0;
    private T[] items;
    private int start = 0, end;

    public ArrayDeque() {
        items = (T[]) new Object[8];
        end = items.length - 1;
    }

    public int size() {
        return size;
    }

    /**
     * resize the array according to capacity and also makes the circular array linear
     * (eg. {5,6,7,1,2} -> {1,2,5,6,7,null...5 times} ) (not sorting)
     *
     * @param capacity - the number to which array should be resized
     */
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];

        if (start < end) {
            System.arraycopy(items, start, a, 0, size);
        } else {
            System.arraycopy(items, start, a, 0, items.length - start);
            System.arraycopy(items, 0, a, items.length - start, end + 1);
        }

        start = 0;
        end = size - 1;
        items = a;
    }

    public void addFirst(T i) {
        if (size == items.length) {
            resize(items.length * 2);
        }

        if (start == 0) {
            start = items.length - 1;
        } else {
            --start;
        }


        items[start] = i;
        ++size;
    }

    public void addLast(T i) {
        if (size == items.length) {
            resize(items.length * 2);
        }

        if (end == items.length - 1) {
            end = 0;
        } else {
            ++end;
        }

        items[end] = i;
        ++size;
    }

    /**
     * Reduces items size if usage < 25%
     */
    private void reduceUsage() {
        if ((items.length >= 16) && (size < items.length / 4)) {
            resize(items.length / 4);
        }
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        T temp = items[start];
        items[start] = null;

        if (start == items.length - 1) {
            start = 0;
        } else {
            ++start;
        }

        --size;
        reduceUsage();
        return temp;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        T temp = items[end];
        items[end] = null;

        if (end == 0) {
            end = items.length - 1;
        } else {
            --end;
        }

        --size;
        reduceUsage();
        return temp;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        if (index + start < items.length) {
            return items[index + start];
        } else {
            return items[index - (items.length - start)];
        }
    }

    public void printDeque() {
        if (isEmpty()) {
            return;
        }

        if (start <= end) {
            for (int i = start; i <= end; ++i) {
                System.out.print(items[i] + " ");
            }
        } else {
            for (int i = start; i < items.length; ++i) {
                System.out.print(items[i] + " ");
            }

            for (int i = 0; i <= end; ++i) {
                System.out.print(items[i] + " ");
            }
        }

        System.out.println();
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayListIterator();
    }

    private class ArrayListIterator implements Iterator<T> {
        int currentPos = 0;

        @Override
        public boolean hasNext() {
            return currentPos < size;
        }

        @Override
        public T next() {
            ++currentPos;
            return get(currentPos - 1);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        Deque<T> other = (Deque<T>) o;

        if (this.size() != other.size()) {
            return false;
        }

        boolean isEqual;
        for (int i = 0; i < size(); ++i) {
            isEqual = get(i).equals(other.get(i));

            if (!isEqual) {
                return false;
            }
        }

        return true;
    }
}
