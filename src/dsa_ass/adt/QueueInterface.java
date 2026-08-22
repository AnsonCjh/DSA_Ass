package dsa_ass.adt;


/**

 * Generic Queue ADT interface.

 * Defines the operations supported by a FIFO queue without exposing

 * implementation details.

 *

 * @param <T> the type of entries stored in the queue

 */

public interface QueueInterface<T> {


    void enqueue(T newEntry);


    T dequeue();


    T peek();


    int size();


    boolean isEmpty();


    void clear();


    T get(int index);


    T remove(int index);

}