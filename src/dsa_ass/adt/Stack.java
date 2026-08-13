package dsa_ass.adt;

/**
 * Generic Stack ADT (Last-In, First-Out - LIFO)
 * Custom implementation using nodes for TARUMT Resort System
 */
public class Stack<T> {

    private Node<T> top;
    private int size;

    public Stack() {
        top = null;
        size = 0;
    }

    // ── Primary Stack Operations ──────────────────────────────────

    /**
     * Push: Adds an element to the top of the stack (LIFO).
     * @param data Element to add
     */
    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * Pop: Removes and returns the element at the top of the stack.
     * @return Element at top of stack, or null if empty
     */
    public T pop() {
        if (isEmpty()) {
            return null;
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * Peek: Retrieves, but does not remove, the top element.
     * @return Element at top of stack, or null if empty
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return top.data;
    }

    /**
     * Size: Returns the number of elements in the stack.
     * @return Stack size
     */
    public int size() {
        return size;
    }

    /**
     * IsEmpty: Checks if the stack is empty.
     * @return true if stack contains 0 items
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear: Empties the stack.
     */
    public void clear() {
        top = null;
        size = 0;
    }

    // ── Utility / Traversal Helper Methods ─────────────────────────

    /**
     * Get element at 0-based index from top to bottom.
     * @param index 0-based position
     * @return Element at index or null if index invalid
     */
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        Node<T> curr = top;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.data;
    }

    /**
     * Remove element at specific 0-based index from top.
     * @param index 0-based position
     * @return Removed element or null if index invalid
     */
    public T remove(int index) {
        if (index < 0 || index >= size) return null;
        T removed;
        if (index == 0) {
            return pop();
        } else {
            Node<T> curr = top;
            for (int i = 0; i < index - 1; i++) {
                curr = curr.next;
            }
            removed = curr.next.data;
            curr.next = curr.next.next;
            size--;
        }
        return removed;
    }

    /**
     * Returns top node for traversal.
     * @return Top Node
     */
    public Node<T> getTop() {
        return top;
    }
}
