package dsa_ass.adt;

/**
 * Generic Singly Linked List ADT
 * Used across all modules of TARUMT Resort System
 */
public class LinkedList<T> {

    private Node<T> head;
    private int size;

    public LinkedList() {
        head = null;
        size = 0;
    }

    // ── Add to end ──────────────────────────────────────────────
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
        } else {
            Node<T> curr = head;
            while (curr.next != null) {
                curr = curr.next;
            }
            curr.next = newNode;
        }
        size++;
    }

    // ── Add at index (0-based) ───────────────────────────────────
    public boolean addAt(int index, T data) {
        if (index < 0 || index > size) return false;
        Node<T> newNode = new Node<>(data);
        if (index == 0) {
            newNode.next = head;
            head = newNode;
        } else {
            Node<T> curr = head;
            for (int i = 0; i < index - 1; i++) {
                curr = curr.next;
            }
            newNode.next = curr.next;
            curr.next = newNode;
        }
        size++;
        return true;
    }

    // ── Get element at index ────────────────────────────────────
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        Node<T> curr = head;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        return curr.data;
    }

    // ── Remove by index ─────────────────────────────────────────
    public T remove(int index) {
        if (index < 0 || index >= size) return null;
        T removed;
        if (index == 0) {
            removed = head.data;
            head = head.next;
        } else {
            Node<T> curr = head;
            for (int i = 0; i < index - 1; i++) {
                curr = curr.next;
            }
            removed = curr.next.data;
            curr.next = curr.next.next;
        }
        size--;
        return removed;
    }

    // ── Replace element at index ────────────────────────────────
    public boolean set(int index, T data) {
        if (index < 0 || index >= size) return false;
        Node<T> curr = head;
        for (int i = 0; i < index; i++) {
            curr = curr.next;
        }
        curr.data = data;
        return true;
    }

    // ── Search (returns first index found, -1 if not) ───────────
    public int indexOf(T data) {
        Node<T> curr = head;
        int index = 0;
        while (curr != null) {
            if (curr.data.equals(data)) return index;
            curr = curr.next;
            index++;
        }
        return -1;
    }

    // ── Contains ─────────────────────────────────────────────────
    public boolean contains(T data) {
        return indexOf(data) != -1;
    }

    // ── Size / empty ─────────────────────────────────────────────
    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    // ── Clear all ────────────────────────────────────────────────
    public void clear() {
        head = null;
        size = 0;
    }

    // ── Get head node (for traversal) ────────────────────────────
    public Node<T> getHead() { return head; }
}
