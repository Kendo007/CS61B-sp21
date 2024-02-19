package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }

        @Override
        public boolean equals(Object obj) {
            Node n = (Node) obj;
            return key.equals(n.key) && value.equals(n.value);
        }
    }

    // Instance Variables
    private Collection<Node>[] buckets;
    /** Number of elements in the hashmap */
    private int N;
    /** LoadFactor of the hashmap */
    private final double loadFactor;
    /** size of the bucket */
    private int M;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        loadFactor = maxLoad;
        this.M = initialSize;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    @Override
    public void clear() {
        buckets = createTable(M);
        N = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** Returns the code of the key passed by which it should be added in bucket */
    private int getCode(K key) {
        return Math.abs(key.hashCode()) % M;
    }

    @Override
    public V get(K key) {
        int keyCode = getCode(key);

        if (buckets[keyCode] == null) {
            return null;
        }

        for (Node i : buckets[keyCode]) {
            if (key.equals(i.key)) {
                return i.value;
            }
        }

        return null;
    }

    @Override
    public int size() {
        return N;
    }

    /** Resizes the buckets arrays when N/M > loadFactor */
    private void resize() {
        Collection<Node>[] temp = buckets;

        M *= 2;
        buckets = createTable(M);
        N = 0;

        for (Collection<Node> i : temp) {
            if (i != null) {
                for (Node j : i) {
                    this.put(j.key, j.value);
                }
            }
        }
    }

    @Override
    public void put(K key, V value) {
        int keyCode = getCode(key);
        boolean replaced = false;

        if (buckets[keyCode] == null) {
            buckets[keyCode] = createBucket();
        }

        for (Node i : buckets[keyCode]) {
            if (key.equals(i.key)) {
                i.value = value;
                replaced = true;
                break;
            }
        }

        if (!replaced) {
            ++N;
            buckets[keyCode].add(createNode(key, value));

            if ((float) N / M > loadFactor) {
                resize();
            }
        }
    }

    @Override
    public Set<K> keySet() {
        HashSet<K> hs = new HashSet<>();

        for (Collection<Node> i : buckets) {
            if (i != null) {
                for (Node j : i) {
                    hs.add(j.key);
                }
            }
        }

        return hs;
    }

    @Override
    public V remove(K key) {
        V value = this.get(key);

        return remove(key, value);
    }

    @Override
    public V remove(K key, V value) {
        int keyCode = getCode(key);

        if (buckets[keyCode].remove(createNode(key, value))) {
            return value;
        } else {
            return null;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}
