/**
 * This class implements a simple FIFO (first-in-first-out) queue simulation.
 * There is a fixed number of servers from the start that serves the 
 * customers and doesn't change throughout the day.
 * 
 * @author Christos M Delivorias
 * @version 25/12/10
 */

public class Simulate2 {

	public static void main(String[] args) {
		double lambdaA = 58.0;
		double lambdaS = 20.0;
		double tEnd = 8.0;
		int nserv = 3;

		simulateNServers(lambdaA, lambdaS, tEnd, nserv);

	}

	/**
	 * Static method to run a simulation with certain parameters. It outputs the
	 * following statistical data.
	 * 
	 * Average waiting time   : 
	 * Server free fraction   : [The percentage of the servers being free]
	 * Maximum queue length   : 
	 * Total customers arrived: [Total arrivals of customers within the work-day]
	 * Total customers served : [Total customers served within the work-day]
	 * Total customers >6min  : [Total number of customers that waited more than 6 minutes to be served, within the work-day]
	 * fraction               : [Percentage of customers that waited more than 6 minutes in the queue]
	 * 
	 * @param lambdaA
	 *            Mean of exponential inter-arrival distribution.
	 * @param lambdaS
	 *            Mean of erlang completion distribution.
	 * @param tEnd
	 *            The time until the end of the work-day
	 * @param nserv
	 *            The number of servers in the branch
	 */
	public static void simulateNServers(double lambdaA, double lambdaS,
			double tEnd, int nserv) {
		Queue q = new Queue(); // Initializes up the Queue
		boolean[] serverFree = new boolean[nserv]; // List of servers of size
													// nserv
		MyRandom r = new MyRandom(12342); // Initializes the random number
		// generator
		int k = 3; // Initializes the kappa of the Erlang distribution

		double clock = 0; // Start of time
		double tna = 0.0; // Time to next arrival
		double[] tnc = new double[nserv]; // Times of size nserv of completion
											// for every server [i]
		double endTime = tEnd; // Minutes until endtime
		int te = 0; // Number of total events
		int ta = 0; // Number of total arrivals
		double tne = 0.0; // The time to the next event
		int qLength = 0; // Max Q length

		double timeFree = 0.0; // Time free at each instance
		double ttServer = 0.0; // Total server free time
		double ttServerFree = 0.0;
		double ttwait = 0.0; // Total waiting time
		double ttServed = 0.0; // Total Number of Customers
		double ttWait6 = 0.0; // Number of customers waiting more than 6 minutes

		// Initialize the servers' status
		for (int i = 0; i < serverFree.length; i++) {
			serverFree[i] = true;
		}
		// Initialize the servers' completion times
		for (int i = 0; i < tnc.length; i++) {
			if (i == 0) {
				tnc[i] = 0.0;
			}
			tnc[i] = Double.POSITIVE_INFINITY;
		}

		// Primary simulation loop
		while (true) {
			boolean arrivalEvent = false; // The type of the next event, arrival
											// or otherwise
			boolean serviceEvent = false; // The type of the next event, service
											// or otherwise
			// Keep track of previous event for server free statistics
			double prevEvent = 0.0;
			// Check if the length of the Q is the largest
			if (q.length() > qLength) {
				qLength = q.length();
			}

			// Get the next completion time from all servers
			int tnc_index;
			tnc_index = getNextTNC(tnc);
			// Check if the next event is a feasible arrival time
			tne = Math.min(tna, tnc[tnc_index]);
			if (tne == tna && tna != tnc[tnc_index]) {
				arrivalEvent = true;
				serviceEvent = false;
			} else if (tne == tnc[tnc_index] && tna != tnc[tnc_index]) {
				serviceEvent = true;
				arrivalEvent = false;
			} else if (tna == tnc[tnc_index]) {
				arrivalEvent = true;
			}

			// Update the clock
			if (tne != Double.POSITIVE_INFINITY) {
				prevEvent = clock;
				clock = tne;
			}

			// Get the index of the next available server
			int id = getFreeServer(serverFree);

			// BRANCH_1// Check if the condition to stop the simulation has been
			// met.
			if (q.isEmpty() && clock >= endTime
					&& tnc[tnc_index] == Double.POSITIVE_INFINITY) {
				System.out.println("Average waiting time: " + ttwait
						/ ttServed);
				System.out.println("Server free fraction: " + ttServer
						/ (clock*nserv));
				System.out.println("Maximum queue length: " + qLength);
				System.out.println("Total customers arrived: " + ta);
				System.out
						.println("Total customers served : " + (int) ttServed);
				System.out.println("Total customers >6min  : " + (int) ttWait6);
				System.out.println(" fraction              : "
						+ (ttWait6 / ttServed));
				System.exit(0);
			}

			// BRANCH_2// This is an arrival event and the end time is not
			// reached
			if (arrivalEvent && clock < endTime) {
				if (id != -1)
					timeFree = clock - prevEvent;
				ttServerFree += timeFree;
				ttServer += timeFree * freeServersNo(serverFree);
				// Report how many servers are free and for how long
				if (freeServersNo(serverFree) != 0)
					System.out.println(freeServersNo(serverFree)
							+ " server(s) are free for " + timeFree
							+ " hours. Total= " + ttServer);

				// Sample for next arrival time so long that it doens't exceed
				// endTime
				tna = clock + r.nextExponential(lambdaA);
				if (tna > tEnd) {
					tna = Double.POSITIVE_INFINITY;
				}

				ta++;
				// BRANCH_3_YES// Inner conditional loop to check if there is at
				// least
				// one server free
				if (id != -1) {
					// Mark the server as busy
					serverFree[id] = false;
					// Sample for time of next completion
					tnc[id] = clock + r.nextErlang(k, lambdaS);

				} else {// BRANCH_3_NO//
						// Put person in the queue
					q.put(clock);
				}
				// Print out the arrival event
				System.out.println(te + "  " + "arrival" + "  " + q.length()
						+ " " + clock);
				if (q.isEmpty()) {
					// System.out.println("Guy is served, has waited: 0.0");
					ttServed++;
				}
			} else {
				// BRANCH_4_YES// This is a service completion event
				if (serviceEvent) {
					if (id != -1)
						timeFree = clock - prevEvent;
					ttServerFree += timeFree;
					ttServer += timeFree * freeServersNo(serverFree);
					// Report how many servers are free and for how long
					if (freeServersNo(serverFree) != 0)
						System.out.println(freeServersNo(serverFree)
								+ " server(s) are free for " + timeFree
								+ " hours. Total= " + ttServer);
					// Update the server time with the sum of all the servers.
					// BRANCH_5_YES// Inner conditional loop to check if the
					// queue is
					// empty
					if (q.isEmpty()) {
						// Get the first busy server and release them
						int busy = getNextTNC(tnc);
						if (busy != -1)
							serverFree[busy] = true;

						if (busy != -1)
							tnc[busy] = Double.POSITIVE_INFINITY;

					}// End of yes in Condition 5
					else {// BRANCH_5_NO//
							// Get person from queue
						double t = q.get();

						// Log the served customer
						ttServed++;
						// Check which customers waited more than 6 minutes (0.1
						// of the hour)
						if ((clock - t) > 0.1) {
							// If it took more than 6 minutes to serve, log it.
							ttWait6++;
						}
						System.out.println("Guy is served, has waited: "
								+ (clock - t));
						// Update the sum of waiting times
						ttwait += (clock - t);

						// Sample for time of next completion
						tnc[tnc_index] = clock + r.nextErlang(k, lambdaS);

					}// End of no in Condition 5
						// Write out the Completion of the event
					int event = tnc_index + 1;
					System.out.println(te + "  " + "compl_" + event + "  "
							+ q.length() + " " + clock);

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

	/**
	 * Find the first server that is free.
	 * @param serverFree The array in which to look for a free server
	 * @return	The index of the position of the first free server
	 */
	private static int getFreeServer(boolean[] serverFree) {
		for (int i = 0; i < serverFree.length; i++) {
			if (serverFree[i] == true) {
				return i;
			}
		}
		return -1;
	}// End of getFreeServer method

	/**
	 * Find the first server that is busy.
	 * @param serverFree The array in which to look for a busy server
	 * @return The index of the position of the first busy server
	 */
	private static int getBusyServer(boolean[] serverFree) {
		for (int i = 0; i < serverFree.length; i++) {
			if (serverFree[i] == false) {
				return i;
			}
		}
		return -1;
	}// End of getFreeServer method

	/**
	 * Count the free servers
	 * @param serverFree The array in which to look for a free server
	 * @return The number of servers that are free
	 */
	private static int freeServersNo(boolean[] serverFree) {
		int servers = 0;
		for (int i = 0; i < serverFree.length; i++) {
			if (serverFree[i] == true) {
				servers++;
			}
		}
		return servers;
	}// End of freeServersNo method

	/**
	 * Find the next completion time
	 * @param tnc_min The completion times array to search in
	 * @return The index of the lowest time of completion
	 */
	private static int getNextTNC(double[] tnc) {
		double tnc_min = Double.POSITIVE_INFINITY;
		int index = 0;
		for (int i = 0; i < tnc.length; i++) {
			if (tnc[i] < tnc_min) {
				index = i;
				tnc_min = tnc[i];
			}
		}
		return index;
	}// End of getNextTNC method
}// End of Class
