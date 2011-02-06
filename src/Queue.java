/**
   This class implements a simple FIFO (first-in-first-out) stack of 
   double values that can be used as a queue.
   @author Andreas Grothey
   @version 08/01/03
 */
public class Queue{

    double [] list;
    int next_on_q;
    int next_off_q;
    int mx_q;

    /**
       Constructor: default maximum queue length is 5000
    */
    public Queue(){
	mx_q = 5000;
	list = new double[mx_q];
	next_on_q = 0;
	next_off_q = 0;
    }

    /** 
	Put a double value (i.e. arrival time) on the queue
	@param clock value to be put on queue (arrival time of customer)
	@exception IndexOutOfBoundsException if maximal queue length reached (5000 by default)
    */
    public void put(double clock){
	if (next_on_q>=mx_q-1) {
	    throw new IndexOutOfBoundsException("Maximal Queue size reached");
	}
	list[next_on_q++] = clock;
    }

    /** 
	Take a value off the queue
	@return next double value from queue (arrival time of next customer in queue) 
	@exception IndexOutOfBoundsException if queue is empty
    */
    public double get(){
	if (next_off_q>=next_on_q){
	    throw new IndexOutOfBoundsException("No one in queue");
	}
	return list[next_off_q++];
    }

    /**
       Returns the number of values (customers) currently in the queue
       @return length of queue
    */
    public int length(){
	return next_on_q - next_off_q;
    }

    /**
       Returns true if queue is empty
       @return true is queue is empty, false otherwise
     */
    public boolean isEmpty(){
	return (length()==0);
    }

    /**
     * Prints the current state of the Queue
     */
    public void print(){
      System.out.println("Length of Queue: "+length());
      for(int i=next_off_q;i<next_on_q;i++){
	System.out.printf("Position %3d arrived at %9.5f\n",
			  i-next_off_q+1, list[i]);

      }
    }
}
