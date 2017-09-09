/*
 * LockFreeQueue.java
 *
 * Created on December 29, 2005, 2:05 PM
 *
 * The Art of Multiprocessor Programming, by Maurice Herlihy and Nir Shavit.
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 20065 Elsevier Inc. All rights reserved.
 */

package queue;

import java.util.concurrent.atomic.AtomicReference;

import queue.SLQueue.Node;
/**
 * Lock-free queue.
 * Based on Michael and Scott http://doi.acm.org/10.1145/248052.248106
 * @param T item type
 * @author Maurice Herlihy
 */
public class LQueue<T> implements Queue<T> {
  private AtomicReference<Node> head;
  private AtomicReference<Node> tail;
  public LQueue() {
    Node sentinel = new Node(null);
    this.head = new AtomicReference<Node>(sentinel);
    this.tail = new AtomicReference<Node>(sentinel);
  }
  /**
   * Append item to end of queue.
   * @param item
   */
  public void enq(T item) {
	  if (item == null) throw new NullPointerException();
	    
	    Node node = new Node(item); // allocate & initialize new node
	    
	    while (true) {		 // keep trying
	      //last and next are thread local variables	
	      Node last = tail.get();    // read tail
	      
	      //we always enqueue at the tail
	      Node next = last.next.get(); // read next
	      
	      //check if thread's last is actual last
	      if(last == tail.get()){
	    	  //make sure next and last.next are same
	    	  //to make sure no one enqueued in my slot
	    	  if(next==null){
	    		  if(last.next.compareAndSet(next, node)){
	    			  //swing pointer
	    			  tail.compareAndSet(last, node);
	    			  return;
	    		  }
	    	  }  else{
	    			  //someone enqueued in my slot
	    			  //do community service:
	    			  //if tail is not updated then I will update
	    			  tail.compareAndSet(last, next);
	    			  //now try again
	    		  }
	    		  
	    	  }
	      }
  }
  /**
   * Remove and return head of queue.
   * @return remove first item in queue
   * @throws queue.EmptyException
   */
  public T deq() throws EmptyException {
    while (true) {
      Node first = head.get();
      Node last = tail.get();
      Node next = first.next.get();
      if (first == head.get()) {// are they consistent?
        if (first == last) {	// is queue empty or tail falling behind?
          if (next == null) {	// is queue empty?
            throw new EmptyException();
          }
          // tail is behind, try to advance
          tail.compareAndSet(last, next);
        } else {
          T value = next.value; // read value before dequeuing
          if (head.compareAndSet(first, next))
            return value;
        }
      }
    }
  }
  
  public int queueLength(){
	  Node ref = head.get();
	  Node lenCk = ref.next.get();
	  int i=0;
	  while(lenCk!=null){
		  i++;
		  lenCk=lenCk.next.get();
	  }
	  return i;
	  
  }
  
  public class Node {
    public T value;
    public AtomicReference<Node> next;
    
    public Node(T value) {
      this.value = value;
      this.next  = new AtomicReference<Node>(null);
    }
  }
  private static <T> boolean multiCompareAndSet(
      AtomicReference<T>[] target,
      T[] expect,
      T[] update) {
    return true;
  }
}
