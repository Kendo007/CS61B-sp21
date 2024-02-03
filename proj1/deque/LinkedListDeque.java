package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private final Node sentinel;
    private int size = 0;

    private class Node {
        private T data;
        private Node before;
        private Node after;

        private Node(T i, Node b, Node a) {
            this.data = i;
            this.before = b;
            this.after = a;
        }
    }

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.after = sentinel;
        sentinel.before = sentinel;
    }

    public int size() {
        return size;
    }

    public void addFirst(T i) {
        sentinel.after.before = new Node(i, sentinel, sentinel.after);
        sentinel.after = sentinel.after.before;

        ++size;
    }

    public void addLast(T i) {
        sentinel.before.after = new Node(i, sentinel.before, sentinel);
        sentinel.before = sentinel.before.after;

        ++size;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        T temp = sentinel.after.data;

        sentinel.after = sentinel.after.after;
        sentinel.after.before = sentinel;

        --size;
        return temp;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        T temp = sentinel.before.data;

        sentinel.before = sentinel.before.before;
        sentinel.before.after = sentinel;

        --size;
        return temp;
    }

    public T get(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        Node temp = sentinel;

        if (index < size / 2) {
            for (int i = 0; i <= index; ++i) {
                temp = temp.after;
            }
        } else {
            for (int i = 0; i < size - index; ++i) {
                temp = temp.before;
            }
        }

        return temp.data;
    }

    private T helperGetRecursiveAfter(int index, Node temp, int n) {
        if (index == n) {
            return temp.data;
        }

        return helperGetRecursiveAfter(index, temp.after, n + 1);
    }

    private T helperGetRecursiveBefore(int index, Node temp, int n) {
        if (index == n) {
            return temp.data;
        }

        return helperGetRecursiveBefore(index, temp.before, n + 1);
    }

    public T getRecursive(int index) {
        if (index >= size || index < 0) {
            return null;
        }

        if (index < size / 2) {
            return helperGetRecursiveAfter(index, sentinel.after, 0);
        } else {
            return helperGetRecursiveBefore(size - index - 1, sentinel.before, 0);
        }
    }

    public void printDeque() {
        if (isEmpty()) {
            return;
        }

        Node temp = sentinel.after;

        while (temp != sentinel) {
            System.out.print(temp.data + " ");
            temp = temp.after;
        }
        System.out.println();
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private Node currentNode = sentinel;

        @Override
        public boolean hasNext() {
            return currentNode.after != sentinel;
        }

        @Override
        public T next() {
            currentNode = currentNode.after;
            return currentNode.data;
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

        try {
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
        } catch (ClassCastException) {
            return false;
        }
    }
}
