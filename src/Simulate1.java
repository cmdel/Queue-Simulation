/**
 * This class implements a simple FIFO (first-in-first-out) queue simulation.
 * There is a single server for all the customers.
 * 
 * @author Christos M Delivorias
 * @version 25/12/10
 */

public class Simulate1 {

	public static void main(String[] args) {
		double lambdaA = 19.0;
		double lambdaS = 20.0;
		double tEnd = 8.0;

		simulateSingleServer(lambdaA, lambdaS, tEnd);

	}

	/**
	 * Static method to run a simulation with certain parameters. It outputs the
	 * following statistical data.
	 * Average waiting time: 
	 * Server free fraction: [The percentage of the servers being free]
	 * Maximum queue length: 
	 * tt_arrivals         : [Total arrivals of customers within the work-day]
	 * tt_wait_time        : [Total waiting time of customers within the work-day]
	 * tt_served           : [Total customers served within the work-day]
	 * tt wait>6min        : [Total number of customers that waited more than 6 minutes to be served, within the work-day]
	 * fraction > 6min     : [Percentage of customers that waited more than 6 minutes in the queue]
	 * 
	 * @param lambdaA
	 *            Mean of exponential inter-arrival distribution.
	 * @param lambdaS
	 *            Mean of erlang completion distribution.
	 * @param tEnd The time until the end of the work-day
	 */
	public static void simulateSingleServer(double lambdaA, double lambdaS,
			double tEnd) {
		Queue q = new Queue(); // Initializes up the Queue
		boolean serverFree = true; // Initializes the server status
		MyRandom r = new MyRandom(12342); // Initializes the random number
											// generator
		int k = 3; // Initializes the kappa of the Erlang distribution

		double clock = 0; // Start of time
		double tna = 0.0; // Time to next arrival
		double tnc = 0.0; // Times of completion for the server
		double endTime = tEnd; // Minutes until endtime
		int te = 0; // Number of total events
		int ta = 0; // Number of total arrivals
		double tne = 0.0; // The time to the next event
		int qLength = 0; // Max Q length

		double timeFree = 0.0; // Time free at each instance
		double ttServer = 0.0; // Total server free time
		double ttwait = 0.0; // Total waiting time
		double ttServed = 0.0; // Total Number of Customers
		double ttWait6 = 0.0; // Number of customers waiting more than 6 minutes
		double fraction = 0.0; // Fraction in percentage of people waiting more
								// than 6 minutes

		// Initializes the printout
		System.out.println("#  event  ql       clock");
		System.out.println("-----------------------------------");

		// Primary simulation loop
		while (true) {
			boolean arrivalEvent = false; // The type of the next event, arrival
											// or otherwise
			boolean serviceEvent = false; // The type of the next event, arrival
											// or otherwise
			// Keep track of previous event for server free statistics
			double prevEvent = 0.0;
			// Check if the length of the Q is the largest
			if (q.length() > qLength) {
				qLength = q.length();
			}

			// Check if the next event is a feasible arrival time
			tne = Math.min(tna, tnc);
			if (tne == tna && tna != tnc) {
				arrivalEvent = true;
				serviceEvent = false;
			} else if (tne == tnc && tna != tnc) {
				serviceEvent = true;
				arrivalEvent = false;
			} else if (tna == tnc) {
				arrivalEvent = true;
			}

			// Update the clock time
			if (tne != Double.POSITIVE_INFINITY) {
				prevEvent = clock;
				clock = tne;
			}

			// BRANCH_1// Check if the condition to stop the simulation has been
			// met.
			if (q.isEmpty() && clock >= endTime
					&& tnc == Double.POSITIVE_INFINITY) {
				System.out
						.println("Average waiting time: " + ttwait / ttServed);
				System.out.println("Server free fraction: " + ttServer / clock);
				System.out.println("Maximum queue length: " + qLength);
				System.out.println("tt_arrivals         : " + ta);
				System.out.println("tt_wait_time        : " + ttwait);
				System.out.println("tt_served           : " + (int) ttServed);
				System.out.println("tt wait>6min        : " + (int) ttWait6);
				System.out.println("fraction > 6min     : "
						+ (ttWait6 / ttServed) * 100 + "%");
				System.exit(0);
			}

			// BRANCH_2// This is an arrival event and the end time is not
			// reached
			if (arrivalEvent && clock < endTime) {
				// Sample for next arrival time
				tna = clock + r.nextExponential(lambdaA);

				ta++;
				// BRANCH_3// Inner conditional loop to check if the server is
				// free
				if (serverFree) {
					// Mark the server as busy
					serverFree = false;
					timeFree = clock - prevEvent;
					ttServer += timeFree;
					// Sample for time of next completion
					tnc = clock + r.nextErlang(k, lambdaS);
					System.out.println("Server is free for: " + timeFree
							+ "  tt server free yet: " + ttServer);
				} else {
					// Put person in the queue
					q.put(clock);
				}
				// Print out the arrival event
				System.out.println(te + "  " + "Arivl" + "  " + q.length()
						+ "  " + clock);
				if (q.isEmpty()) {
					// Print out waiting time for customer who just arrived o an
					// empty queue
					System.out.println("Guy is served, has waited: 0.0");
					ttServed++;
				}
			} else {
				// BRANCH_4// This is a service completion event
				if (serviceEvent) {
					// Write out the Completion of the event
					System.out.println(te + "  " + "Compl" + "  " + q.length()
							+ "  " + clock);
					// BRANCH_5_YES// Inner conditional loop to check if the
					// queue is empty
					if (q.isEmpty()) {
						serverFree = true;
						tnc = Double.POSITIVE_INFINITY;

					}// End of yes in Condition 5
					else {// BRANCH_5_NO//
						double t = q.get();
						// Log the served customer
						ttServed++;
						// Check which customers waited more than 6 minutes (0.1
						// of the hour)
						if ((clock - t) > 0.1) {
							// If it took more than 6 minutes to serve, log it.
							ttWait6++;
						}
						// Print out waiting time for customer
						System.out.println("Guy is served, has waited: "
								+ (clock - t));
						// Update the sum of waiting times
						ttwait += (clock - t);
						tnc = clock + r.nextErlang(k, lambdaS);
					}// End of no in Condition 5

				}// End of Condition 4
				else {// BRANCH_4_NO//
						// Set next arrival time to very large number
					tna = Double.POSITIVE_INFINITY;
				}// End of Infinite arrival time
			}// End of no in Condition 2
				// Increment the number of events
			te++;
		}// End of while loop
	}// End of static main method
}// End of Class
