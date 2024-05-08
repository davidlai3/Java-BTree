package cse214hw3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Arrays;
import java.util.Collection;

public class BTree<E extends Comparable<E>> {
    private final int MIN;
    private Node root;
    private class Node {
        private boolean leaf;
        private int numVals;
        private final ArrayList<E> vals;
        private final ArrayList<Node> children;

        public Node() {
            this.leaf = true;
            this.numVals = 0;
            this.vals = new ArrayList<>(); // Sized at (2*MIN) - 1
            this.children = new ArrayList<>(); // Sized at (2*MIN)
        }

        public Node(ArrayList<E> vals, ArrayList<Node> children) {
            this.leaf = true;
            this.numVals = vals.size();
            this.vals = vals; // Sized at (2*MIN) - 1
            this.children = children; // Sized at (2*MIN)
        }

        public String toString() {
            StringBuilder res = new StringBuilder();
            res.append("[");
            for (E element : this.vals) {
                res.append(element.toString());
                res.append(", ");
            }
            if (res.length()>1) {
                res.deleteCharAt(res.length()-1);
                res.deleteCharAt(res.length()-1);
            }
            res.append("]");
            return res.toString();
        }

    }


    public class NodeAndIndex {
        private Node node;
        private int index;

        public NodeAndIndex(Node node, int index) {
            this.node = node;
            this.index = index;
        }
        public String toString() {
            return node.toString() + " at index " + index;
        }
    }

    public BTree(int minimumDegree) {
        if (minimumDegree <= 0) {
            throw new IllegalArgumentException();
        }
        this.MIN = minimumDegree;
        this.root = new Node();
    }

    public void add(E element) {
        // If element already exists then we do nothing
        if (this.find(element) != null) {
            return;
        }
        // If root is full:
        if (root.vals.size() == (2*MIN)-1) {
            splitRoot();
            insertNonFull(element, root);
        }
        else {
            insertNonFull(element, root);
        }
    }

    private void splitRoot() {
        // Create new node and move median up
        E median = root.vals.get(MIN-1);
        ArrayList<E> rootVals = new ArrayList<>();
        ArrayList<Node> rootChildren = new ArrayList<>();
        rootVals.add(median);
        root.vals.remove(MIN-1);

        // Split vals into left and right children
        ArrayList<E> left = new ArrayList<E>(root.vals.subList(0, MIN-1));
        ArrayList<E> right = new ArrayList<E>(root.vals.subList(MIN-1, 2*MIN-2));

        // Assign children to new root children
        ArrayList<Node> leftChildren;
        ArrayList<Node> rightChildren;

        if (!this.root.children.isEmpty()) {
            leftChildren = new ArrayList<>(root.children.subList(0,MIN));
            rightChildren = new ArrayList<>(root.children.subList(MIN, 2*MIN));
        }
        else {
            leftChildren = new ArrayList<>();
            rightChildren = new ArrayList<>();
        }

        // Create children nodes
        Node leftChild = new Node(left, leftChildren);
        Node rightChild = new Node(right, rightChildren);

        // If the new children also have children, then they cannot be leaf nodes
        if (!this.root.children.isEmpty()) {
            leftChild.leaf = false;
            rightChild.leaf = false;
        }

        rootChildren.add(leftChild);
        rootChildren.add(rightChild);

        // Create and assign new root
        this.root = new Node(rootVals, rootChildren);
        this.root.leaf = false;
    }


    private void splitChild(Node node, ArrayList<Node> children, int childPos) {
        Node child = children.get(childPos);
        E median = child.vals.get(MIN-1);

        // Insert median to designated position
        int i = 0;
        while (i < node.numVals && median.compareTo(node.vals.get(i)) > 0) {i++;}
        node.vals.add(i, median);
        node.numVals++;

        // Remove median from child
        child.vals.remove(MIN-1);
        child.numVals--;

        // Create values for new nodes
        ArrayList<E> left = new ArrayList<E>(child.vals.subList(0, MIN-1));
        ArrayList<E> right = new ArrayList<E>(child.vals.subList(MIN-1, 2*MIN-2));

        // Create children for new nodes
        ArrayList<Node> leftChildren;
        ArrayList<Node> rightChildren;

        // Assign children to new nodes
        if (!child.leaf) {
            leftChildren = new ArrayList<>(child.children.subList(0,MIN));
            rightChildren = new ArrayList<>(child.children.subList(MIN, 2*MIN));
        }
        else {
            leftChildren = new ArrayList<>();
            rightChildren = new ArrayList<>();
        }

        // Create new nodes
        Node leftNode = new Node(left, leftChildren);
        Node rightNode = new Node(right, rightChildren);
        if (!child.leaf) {
            leftNode.leaf = false;
            rightNode.leaf = false;
        }

        // Insert new children to original node and remove full child
        children.remove(childPos);
        children.add(childPos, leftNode);
        children.add(childPos + 1, rightNode);
    }

    private void insertNonFull(E element, Node node) {
        int i = 0;
        // If we are at a leaf node, simply insert value into designated place
        if (node.leaf) {
            if (node.numVals != 0) {
                while (i < node.numVals && element.compareTo(node.vals.get(i)) > 0) {i++;}
            }
            node.vals.add(i, element);
            node.numVals++;
        }
        // If we are not at a leaf node, we need to traverse to the proper node
        else {
            // Traverse the values of the current node
            while (i < node.numVals && element.compareTo(node.vals.get(i)) > 0) {
                i++;
            }

            // If the proper child is full, split
            if (node.children.get(i).numVals == 2*MIN - 1) {
                splitChild(node, node.children, i);

                // Go to left or right child
                if (element.compareTo(node.vals.get(i)) > 0) {i++;}
            }
            insertNonFull(element, node.children.get(i));


        }
    }

    public NodeAndIndex find(E element) {
        return find(element, this.root);
    }

    private NodeAndIndex find(E element, Node node) {
        // Traverse given node for element
        int i = 0;
        while (i < node.numVals && element.compareTo(node.vals.get(i)) > 0) {
            i++;
        }

        // Case 1: We find the element at the current node
        if (i < node.numVals && element.equals(node.vals.get(i))) {
            return new NodeAndIndex(node, i);
        }
        // Case 2: The element does not exist in the tree
        if (node.leaf) {
            return null;
        }
        // Case 3: We recursively go down to a child of the current node
        return find(element, node.children.get(i));
    }

    // Superfluous Functions

    public void show() {
        String nodesep = " ";
        Queue<Node> queue1 = new LinkedList<>();
        Queue<Node> queue2 = new LinkedList<>();
        queue1.add(root); /* root of the tree being added */
        while (true) {
            while (!queue1.isEmpty()) {
                Node node = queue1.poll();
                System.out.printf("%s%s", node.toString(), nodesep);
                if (!node.children.isEmpty())
                    queue2.addAll(node.children);
            }
            System.out.printf("%n");
            if (queue2.isEmpty())
                break;
            else {
                queue1 = queue2;
                queue2 = new LinkedList<>();
            }
        }
    }

    public void addAll(Collection<E> elements) {
        for (E e : elements) this.add(e);
    }

    public static void main(String[] args) {
        BTree<Integer> tree = new BTree<>(3);
        ArrayList<Integer> data = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            data.add(i);
        }

        //TODO: Make more test cases
        tree.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 11,
                12, 22, 19, 25, 100, 88, 64, 65, 16));

        tree.show();
        System.out.println(tree.find(65));
    }

}
