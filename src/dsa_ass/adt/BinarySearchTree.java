package dsa_ass.adt;

/**
 * Generic Binary Search Tree (BST) ADT
 * Custom implementation for TARUMT Resort System
 */
public class BinarySearchTree<T extends Comparable<T>> {

    private TreeNode<T> root;
    private int size;

    public BinarySearchTree() {
        root = null;
        size = 0;
    }

    // ── Insert / Add Operation ────────────────────────────────────

    /**
     * Inserts an element into the Binary Search Tree.
     * @param data Element to insert
     */
    public void add(T data) {
        root = addRecursive(root, data);
    }

    private TreeNode<T> addRecursive(TreeNode<T> current, T data) {
        if (current == null) {
            size++;
            return new TreeNode<>(data);
        }

        int comp = data.compareTo(current.data);
        if (comp < 0) {
            current.left = addRecursive(current.left, data);
        } else if (comp > 0) {
            current.right = addRecursive(current.right, data);
        } else {
            // Duplicate key - replace existing data
            current.data = data;
        }
        return current;
    }

    // ── Search Operation ──────────────────────────────────────────

    /**
     * Searches for an element in the Binary Search Tree.
     * @param data Target element
     * @return Found element or null if not found
     */
    public T search(T data) {
        return searchRecursive(root, data);
    }

    private T searchRecursive(TreeNode<T> current, T data) {
        if (current == null) return null;
        int comp = data.compareTo(current.data);
        if (comp == 0) return current.data;
        if (comp < 0) return searchRecursive(current.left, data);
        return searchRecursive(current.right, data);
    }

    /**
     * Checks if the BST contains data.
     */
    public boolean contains(T data) {
        return search(data) != null;
    }

    // ── Delete / Remove Operation ─────────────────────────────────

    /**
     * Removes an element from the Binary Search Tree.
     * @param data Element to remove
     * @return true if removed successfully, false otherwise
     */
    public boolean remove(T data) {
        int initialSize = size;
        root = removeRecursive(root, data);
        return size < initialSize;
    }

    private TreeNode<T> removeRecursive(TreeNode<T> current, T data) {
        if (current == null) return null;

        int comp = data.compareTo(current.data);
        if (comp < 0) {
            current.left = removeRecursive(current.left, data);
        } else if (comp > 0) {
            current.right = removeRecursive(current.right, data);
        } else {
            // Found node to delete
            size--;
            // Case 1: Leaf node (no children)
            if (current.left == null && current.right == null) {
                return null;
            }
            // Case 2: One child
            if (current.left == null) {
                return current.right;
            }
            if (current.right == null) {
                return current.left;
            }
            // Case 3: Two children - find in-order successor (min node in right subtree)
            T smallestValue = findMin(current.right);
            current.data = smallestValue;
            // Retain size since removeRecursive below will decrement size again
            size++;
            current.right = removeRecursive(current.right, smallestValue);
        }
        return current;
    }

    private T findMin(TreeNode<T> root) {
        return root.left == null ? root.data : findMin(root.left);
    }

    // ── Utility / Traversal Methods ───────────────────────────────

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Gets element at 0-based in-order traversal index.
     * @param index 0-based index
     * @return Element at index or null
     */
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        Object[] result = new Object[1];
        int[] counter = new int[1];
        inOrderGet(root, index, counter, result);
        return (T) result[0];
    }

    private void inOrderGet(TreeNode<T> node, int targetIndex, int[] counter, Object[] result) {
        if (node == null || result[0] != null) return;
        inOrderGet(node.left, targetIndex, counter, result);
        if (result[0] != null) return;
        if (counter[0] == targetIndex) {
            result[0] = node.data;
            return;
        }
        counter[0]++;
        inOrderGet(node.right, targetIndex, counter, result);
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    // ── Confirmation Number Search ─────────────────────────────────
    //
    // BST Tree-Walk Search by Confirmation Number
    // ─────────────────────────────────────────────────────────────
    // The BST is keyed (ordered) by Reservation ID via compareTo().
    // Confirmation numbers are a secondary, non-key attribute, so we
    // cannot navigate left/right by value. Instead we perform an
    // in-order BST traversal, visiting every node, and return the
    // first node whose confirmationNo field matches the target.
    //
    // This demonstrates the non-linear BST structure: nodes are visited
    // via left-subtree → root → right-subtree recursion, making the
    // tree shape (and O(n) worst-case traversal) explicit.
    //
    // For the purposes of this DSA assignment, reservations are
    // stored in a BST keyed by Reservation ID.  The confirmation-number
    // search is a full tree-walk on that BST — a different and clearly
    // labelled BST operation from the O(log n) binary search above.

    /**
     * BST In-Order Traversal Search by 8-digit Confirmation Number.
     *
     * Walks the entire BST (left → node → right) comparing each
     * Reservation's confirmationNo until a match is found.
     *
     * @param confNo 8-digit confirmation number string (e.g. "12345001")
     * @return the matching Reservation, or null if not found
     */
    public dsa_ass.entity.Reservation searchByConfirmation(String confNo) {
        return searchConfRecursive(root, confNo);
    }

    private dsa_ass.entity.Reservation searchConfRecursive(
            TreeNode<T> node, String confNo) {
        if (node == null) return null;

        // Traverse left subtree first (in-order)
        dsa_ass.entity.Reservation leftResult =
                searchConfRecursive(node.left, confNo);
        if (leftResult != null) return leftResult;

        // Check this node
        if (node.data instanceof dsa_ass.entity.Reservation) {
            dsa_ass.entity.Reservation res =
                    (dsa_ass.entity.Reservation) node.data;
            if (confNo != null && confNo.equals(res.getConfirmationNo())) {
                return res;
            }
        }

        // Traverse right subtree
        return searchConfRecursive(node.right, confNo);
    }
}
