package queue;

public interface Queue<T> {

	public void enq(T item);
	 public T deq() throws EmptyException;
	 public int queueLength();
	
}
