package dsa_ass.adt;

/**
 * Generic Queue ADT (First-In, First-Out - FIFO)
 * Custom implementation using nodes for TARUMT Resort System
 */
public class Queue<T> {

    private Node<T> front;
    private Node<T> rear;
    private int size;

    public Queue() {
        front = null;
        rear = null;
        size = 0;
    }

    // ── Primary Queue Operations ──────────────────────────────────

    /**
     * Enqueue: Adds an element to the rear of the queue (FIFO).
     * @param data Element to add to queue
     */
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /**
     * Dequeue: Removes and returns the element at the front of the queue.
     * @return Element at front of queue, or null if empty
     */
    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    /**
     * Peek: Retrieves, but does not remove, the front element.
     * @return Element at front of queue, or null if empty
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    /**
     * Size: Returns the number of elements in the queue.
     * @return Number of items in queue
     */
    public int size() {
        return size;
    }

    /**
     * IsEmpty: Checks if the queue is empty.
     * @return true if queue contains 0 items, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clear: Empties the queue.
     */
    public void clear() {
        front = null;
        rear = null;
        size = 0;
    }

    // ── Utility / Traversal Helper Methods ─────────────────────────

    /**
     * Get element at 0-based index from front to rear.
     * @param index 0-based position
     * @return Element at index or null if index invalid
     */
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        Node<T> curr = front;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.data;
    }

    /**
     * Remove element at specific 0-based index.
     * @param index 0-based position
     * @return Removed element or null if index invalid
     */
    public T remove(int index) {
        if (index < 0 || index >= size) return null;
        T removed;
        if (index == 0) {
            return dequeue();
        } else {
            Node<T> curr = front;
            for (int i = 0; i < index - 1; i++) {
                curr = curr.next;
            }
            removed = curr.next.data;
            if (curr.next == rear) {
                rear = curr;
            }
            curr.next = curr.next.next;
            size--;
        }
        return removed;
    }

    /**
     * Returns the front node for traversal if needed.
     * @return Front Node
     */
    public Node<T> getFront() {
        return front;
    }
}
