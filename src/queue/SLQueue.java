package queue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import queue.EmptyException;

public class SLQueue<T> implements Queue<T>{
	 
	  private AtomicReference<Node> head;
	  private AtomicReference<Node> tail;
	  private AtomicReference<Node> lenCheck;
	  
	  private ThreadLocal<Random> random = new ThreadLocal<Random>() {
			public Random initialValue() {
				return new Random();
			}
		};
	  
	  private final int n;
	  
	  public SLQueue(int n) {
		//we have a Node class and sentinel object  
	    Node sentinel = new Node(null);
	    this.head = new AtomicReference<Node>(sentinel);
	    this.tail = new AtomicReference<Node>(sentinel);
	    
	    Node lenCk = new Node(null);
	    

	    this.n=n;
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
		
		Random rand = random.get();  
		int loop=0;  
	    while (true) {
	    
	      //first,next and last are thread local variables
	      Node first = head.get();
	      Node last = tail.get();
	      Node next = first.next.get();
	      
	      //is my first still the actual first (no other thread dequeued the head before me)
	      if (first == head.get()) {// are they consistent?
	        if (first == last) {	// is queue empty or tail falling behind?
	          if (next == null) {	// is queue empty?
	            throw new EmptyException();
	          }
	          // tail is behind, try to advance (it means tail is not empty some item has been enq so update
	          tail.compareAndSet(last, next);
	        } else {
	          
	        	int index=0;
	        	if(loop<100){
	        		
	        		index = rand.nextInt(n);
	        		loop=loop+1;
	        		
	        		
	        		if(index>0){
	        			T value=UnConventionalDequeue(index, next);
	        			if(value!=null){
	        				return value;
	        			}
	        		}
	        		
	        		while(next!=null){
	        			if(next.mark.compareAndSet(false, true)){
	        				return next.value;
	        			}
	        			if(next!=last && head.compareAndSet(first, next)){
	        				first=next;
	        				next=next.next.get();
	        			}else{
	        				break;
	        			}
	     
	        		}
	        	}else{
	        		return null;
	        	}
	          
	        }
	      }
	    }
	  }
	  
	  public T UnConventionalDequeue(int index, Node next){
		  int i=0;
		  while(i<index){
			  if(next.next.get()==null){
				  break;
			  }
			  next=next.next.get();
			  i=i+1;
		  }
		  if(next.mark.compareAndSet(false,true)){
			  return next.value;
		  }else{
			  return null;
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
	    public AtomicBoolean mark;
	    
	    public Node(T value) {
	      this.value = value;
	      this.next  = new AtomicReference<Node>(null);
	      this.mark=new AtomicBoolean(false);
	    }
	  }
}
