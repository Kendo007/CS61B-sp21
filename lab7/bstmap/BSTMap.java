package bstmap;

import java.util.HashSet;
import java.util.Iterator;
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    /** Stores the size of the Map */
    private int size;
    /** Keys and values are stored in a Binary Search Tree of Entry objects.
     *  This variable stores the first pair in this tree root */
    private BSTNode root;
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return root != null && root.get(key) != null;
    }

    @Override
    public V get(K key) {
        if (root == null) {
            return null;
        }

        BSTNode lookup = root.get(key);
        if (lookup == null) {
            return null;
        }

        return lookup.val;
    }

    @Override
    public int size() {
        return size;
    }

    private BSTNode insert(BSTNode node, K key, V value, BSTNode parent) {
        if (node == null) {
            ++size;
            return new BSTNode(key, value, null, null, parent);
        }

        int comparison = key.compareTo(node.key);

        if (comparison < 0) {
            node.leftChild = insert(node.leftChild, key, value, node);
        } else if (comparison > 0) {
            node.rightChild = insert(node.rightChild, key, value, node);
        } else {
            node.val = value;
        }

        return node;
    }

    @Override
    public void put(K key, V value) {
        if (root == null) {
            ++size;
            root = new BSTNode(key, value, null, null, null);
        }

        root = insert(root, key, value, root);
    }

    @Override
    public HashSet<K> keySet() {
        HashSet<K> s = new HashSet<>();

        for (K i : this) {
            s.add(i);
        }

        return s;
    }

    /**
     * Returns the leftmost child
     */
    private BSTNode leftMost(BSTNode node) {
        if (node == null) {
            return null;
        } else if (node.isLeaf()) {
            return node;
        }

        while (node.leftChild != null) {
            node = node.leftChild;
        }

        return node;
    }

    private void removeNode(BSTNode node) {
        BSTNode singleChild = node.singleChild();
        --size;

        if (node.isLeaf()) {
            // Special Case if parent is Root
            if (node.parent == null) {
                root = null;
                return;
            }

            // For Rest
            if (node.isleftChild()) {
                node.parent.leftChild = null;
            } else {
                node.parent.rightChild = null;
            }
        } else if (singleChild != null) {
            // Special Case if parent is root
            if (node.parent == null) {
                if (node.leftChild == null) {
                    root = node.rightChild;
                } else {
                    root = node.leftChild;
                }
                return;
            }

            // For rest
            if (node.isleftChild()) {
                node.parent.leftChild = singleChild;
            } else {
                node.parent.rightChild = singleChild;
            }
        } else {
            BSTNode newRoot = leftMost(node.rightChild);

            if (newRoot.rightChild != null && newRoot.parent != null) {
                newRoot.parent.rightChild = newRoot.rightChild;
            }

            root.key = newRoot.key;
            root.val = newRoot.val;
        }
    }

    @Override
    public V remove(K key) {
        BSTNode node = root.get(key);

        if (node == null) {
            return null;
        }

        V value = node.val;

        removeNode(node);
        return value;
    }

    @Override
    public V remove(K key, V value) {
        BSTNode node = root.get(key);

        if (node == null) {
            return null;
        }

        V testValue = node.val;
        if (!testValue.equals(value)) {
            return null;
        }

        removeNode(node);
        return testValue;
    }

    private class TraverseBinaryTree implements Iterator<K> {
        private BSTNode returnNode = root;
        boolean visitedLeft = false;
        int countNodes = 0;
        boolean rootReturned = false;

        private BSTNode getNext(BSTNode currNode) {
            if (countNodes == size || currNode == null) {
                return null;
            }

            if (currNode.isLeaf() && returnNode != currNode) {
                return currNode;
            }

            if (returnNode == currNode) {
                if (currNode.parent == null) {
                    if (!visitedLeft) {
                        visitedLeft = true;
                        return getNext(leftMost(currNode));
                    } else if (!rootReturned) {
                        rootReturned = true;
                        return currNode;
                    } else {
                        return getNext(leftMost(currNode.rightChild));
                    }
                }

                if (currNode.rightChild == null) {
                    return getNext(currNode.parent);
                } else {
                    return getNext(leftMost(currNode.rightChild));
                }
            } else if (returnNode == currNode.leftChild) {
                return currNode;
            } else if (returnNode == currNode.rightChild) {
                returnNode = currNode;
                return getNext(currNode.parent);
            } else {
                return currNode;
            }
        }

        @Override
        public boolean hasNext() {
            returnNode = getNext(returnNode);
            return returnNode != null;
        }

        @Override
        public K next() {
            ++countNodes;
            return returnNode.key;
        }
    }

    @Override
    public Iterator<K> iterator() {
        return new TraverseBinaryTree();
    }

    private class BSTNode {
        /** Stores the key of the key-value pair of this node in the list. */
        K key;
        /** Stores the value of the key-value pair of this node in the list. */
        V val;
        /** Stores the left Child of the Node */
        BSTNode leftChild;
        /** Stores the right Child of the Node */
        BSTNode rightChild;
        /** Stores the Parent of the Node */
        BSTNode parent;
        BSTNode(K k, V v, BSTNode l, BSTNode r, BSTNode p) {
            key = k;
            val = v;
            leftChild = l;
            rightChild = r;
            parent = p;
        }

        BSTNode get(K k) {
            if (key == null) {
                return null;
            }

            int comparison = k.compareTo(key);

            try {
                if (comparison < 0) {
                    return leftChild.get(k);
                } else if (comparison > 0) {
                    return rightChild.get(k);
                } else {
                    return this;
                }
            } catch (NullPointerException e) {
                return null;
            }
        }

        /**
         * Returns true or false if the given node is a leaf or not
         */
        public boolean isLeaf() {
            return leftChild == null && rightChild == null;
        }

        /**
         * Returns the node if it has a single child else returns null
         */
        public BSTNode singleChild() {
            if (leftChild != null && rightChild == null) {
                return leftChild;
            } else if (leftChild == null && rightChild != null) {
                return rightChild;
            } else {
                return null;
            }
        }

        public boolean isleftChild() {
            return parent.leftChild == this;
        }
    }
}
