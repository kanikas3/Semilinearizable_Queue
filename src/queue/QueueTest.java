
package queue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;



public class QueueTest extends Thread {
	
	private static String QNAME;
	private static int THREAD_COUNT; 
	private static int DURATION;
	private static int N;
	private static boolean stop = false;
	private Random num1 = new Random();
	private int count = 0;
	private int enqcount=0;
	private int deqcount=0;
	private static int finalCount = 0;
	private static int finalenqcount = 0;
	private static int finaldeqcount = 0;
	public static Queue<Integer> slq;	

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		QNAME = args[0];
		THREAD_COUNT = Integer.parseInt(args[1]);
		DURATION = Integer.parseInt(args[2]);
		
		
		if (args.length == 4) {
			N=Integer.parseInt(args[3]);
			slq = new SLQueue<Integer>(N);
			for(int i=0;i<1000;i++){
				slq.enq(i);	
			}
		}else if(args.length == 3){
			slq = new LQueue<Integer>();
			for(int i=0;i<1000;i++){
				slq.enq(i);	
			}
		}else{
			System.out.println("Wrong argument count");
		}	
		
		Thread[] threads = new Thread[THREAD_COUNT];
		
		for(int i=0; i<THREAD_COUNT;i++){
			threads[i] = new QueueTest();
		}
		
		for(int i=0;i<THREAD_COUNT;i++){
			threads[i].start();
		}
		
		try
		{
			Thread.sleep(DURATION);
		}
		finally
		{
			for(Thread thread: threads)
				stop=true;
		}
		
		
		for(int i=0;i<THREAD_COUNT;i++){
			threads[i].join();
		}

		for(int i=0;i<THREAD_COUNT;i++){
			
			finalCount= finalCount+((QueueTest)threads[i]).returnCount();
			finalenqcount = finalenqcount + ((QueueTest)threads[i]).returnenqcount();
			finaldeqcount = finaldeqcount + ((QueueTest)threads[i]).returndeqcount();
		}
		int qLen = slq.queueLength();
		
		
		System.out.println("Queue: "+QNAME+" No of Threads: "+THREAD_COUNT);
		System.out.println("Total enqueues done: "+finalenqcount+"   "+"Total dequeues done: "+finaldeqcount+"   "+"Nodes remaning in queue: "+qLen);
		System.out.println("the number of succesful invocations are "+finalCount);		
		
		Float value = new Float(DURATION);
		Float sec = value/1000;
		Float value2 = new Float(finalCount);
		Float throughput = value2/sec;
		System.out.println("Throughput for this scenario is "+throughput);

	}
	
	public void run(){
		
		while(!stop)
		{
				//create Random number for method ratios
				int item=num1.nextInt(100);
				
				if(item < 50){
					//perform enqueue
					slq.enq(item);
					count++;
					enqcount++;
				}else{
					//perform dequeue
					try {
						if(slq.deq()!=null){
							count++;	
						}
					} catch (EmptyException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
					deqcount++;
				}	
		}	
		
		
		}
	
	public int returnCount(){
		return count;
	}
	public int returnenqcount(){
		return enqcount;
	}
	public int returndeqcount(){
		return deqcount;
	}

}
