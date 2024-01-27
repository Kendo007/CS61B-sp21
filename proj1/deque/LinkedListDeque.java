package deque;

public class LinkedListDeque<Item> {
    private Node sentinel;
    private int size = 0;

    private class Node {
        private Item data;
        private Node before;
        private Node after;

        private Node(Item i, Node b, Node a) {
            this.data = i;
            this.before = b;
            this.after = a;
        }
    }

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    private void addIfEmpty(Item i) {
        sentinel.after = new Node(i, sentinel, sentinel);
        sentinel.before = sentinel.after;
    }

    public void addFirst(Item i) {
        if (isEmpty()) {
            addIfEmpty(i);
        } else {
            sentinel.after.before = new Node(i, sentinel, sentinel.after);
            sentinel.after = sentinel.after.before;
        }

        ++size;
    }

    public void addLast(Item i) {
        if (isEmpty()) {
            addIfEmpty(i);
        } else {
            sentinel.before.after = new Node(i, sentinel.before, sentinel);
            sentinel.before = sentinel.before.after;
        }

        ++size;
    }

    public Item removeFirst() {
        if (isEmpty()) { return null; }

        Item temp = sentinel.after.data;

        sentinel.after = sentinel.after.after;
        sentinel.after.before = sentinel;

        --size;
        return temp;
    }

    public Item removeLast() {
        if (isEmpty()) { return null; }

        Item temp = sentinel.before.data;

        sentinel.before = sentinel.before.before;
        sentinel.before.after = sentinel;

        --size;
        return temp;
    }

    public Item get(int index) {
        if (index >= size || index < 0) { return null; }

        Node temp = sentinel;

        if (index < size / 2) {
            for (int i = 0; i <= index; ++i) { temp = temp.after; }
        } else {
            for (int i = 0; i < size - index; ++i) { temp = temp.before; }
        }

        return temp.data;
    }

    public void printDeque() {
        Node temp = sentinel;

        while (temp.after != sentinel) {
            System.out.print(temp.data + " ");
            temp = temp.after;
        }
        System.out.println();
    }
}
