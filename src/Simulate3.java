import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * This class implements a simple FIFO (first-in-first-out) queue simulation.
 * 
 * @author Christos M Delivorias
 * @version 25/12/10
 */

public class Simulate3 {

	private static boolean openPosition;
	private static double fractionAvg = 0.0;
	private static DecimalFormat percent;


	public static void main(String[] args) {
		double lambdaA = 58.0;
		double lambdaS = 30.0;
		double tEnd = 8.0;
		/**
		 * By starting and finishing shift with 1 server, almost 98.484848% of
		 * customers wait more than 6 mins with the avg wait time at 218 hours.
		 * With 2 servers the avg wait time is 13 hours with 69.1489% waiting
		 * more than 6 mins. With 3 servers the avg waiting time is 26mins with
		 * 0% of customers waiting more than 6 mins. Therefore the optimal
		 * allocation will be by assigning 2 servers to start off the shift and
		 * when the need arises, open a new position of service. If 1 server
		 * alone starts, the fraction in close to 16%. By starting with 2 servers
		 * the fraction is 4.3%.
		 */
		int nserv = 2;
		
		// The starting seed for the random number generator
		long seed = 12342;
		
		//The number of times the simulation will be repeated for the average statistics
		int trials =100;
		
		// Conduct a number of trials in order to get the average fraction of customers
		// waiting more than 6 mins. Use different seed in each trial.
		for (int i = 1; i <= trials; i++) {
			simulateNServers(lambdaA, lambdaS, tEnd, nserv, seed);
			seed++;
		}
		double trialAvg = fractionAvg/trials;
		percent = new DecimalFormat("0.#%");	
		System.out.println("=============================================================================");
		System.out.println();
		System.out.println("Average percent of customers waiting more than 6 minutes (100 trials) : "+percent.format(trialAvg));

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
	 * @param seed 
	 */
	public static void simulateNServers(double lambdaA, double lambdaS,
			double tEnd, int nserv, long seed) {
		Queue q = new Queue(); // Initializes up the Queue
		ArrayList<Server> serverFree = new ArrayList<Server>(); // Create list of servers
		MyRandom r = new MyRandom(seed); // Initializes the random number
		// generator
		int k = 3; // Initializes the kappa of the Erlang distribution

		double clock = 0; // Start of time
		double tna = 0.0; // Time to next arrival
		//double[] tnc = new double[nserv]; // Times of size nserv of completion
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
		int busySrv = 0;	// The total number of servers busy for all events	
		int prevHour = 0;		// Keeping track of which hour has finished
		int hourStartEvent = 0;
		int serverSum = 0;
		double serverHourlyAvg = 0.0;
		
		// Initialize the servers' status
		for (int i = 0; i < nserv; i++) {
			serverFree.add(new Server(true));
		}
		
		// Initialise the first completion time to 0.0
		Server tmpSrv = serverFree.get(0);
		tmpSrv.setTnc(0.0);
		serverFree.set(0, tmpSrv);
		
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
			//Keep track of the fraction of customers waiting more than 6mins
			//This should not exceed 5% on average
			double fraction = ttWait6 / ttServed;
			
			// Print out the fraction per event
			System.out.println("fraction: "
					+ fraction);
			// Prints out the number of servers
			int servers=serverFree.size();
			System.out.println("servers: "
					+ servers);

			// Keep track of how many servers are busy
			busySrv += busyServersNo(serverFree);

			// Get the next completion time from all servers
			double tnc;
			Server tmp = getNextTNC(serverFree); 
			if(tmp!=null) tnc = tmp.getTnc();
			else tnc=Double.POSITIVE_INFINITY;
			int tnc_index = serverFree.indexOf(tmp);
			
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

			// Update the clock
			if (tne != Double.POSITIVE_INFINITY) {
				prevEvent = clock;
				clock = tne;
			}

			/**
			 * In this block I'll be keeping some statistics for the average
			 * allocation of servers per hour
			 */
			int currHour=(int) clock;
			if(currHour==prevHour){
				serverSum+=servers;
				
			}else if (currHour>=prevHour){
				serverHourlyAvg= (double) serverSum/(te-hourStartEvent);
				System.out.println("Avg servers for hour "+currHour+" : "+serverHourlyAvg);
				serverSum = 0;
				hourStartEvent=te;
				prevHour=currHour;
			}
			
			/**
			 * Every time the cycle begins there needs to be a check whether the
			 * optimality constraint is met. This constraint is that the queue
			 * is no larger than 6 customers. If it is not, then the algorithm
			 * needs to set up a flag that will increase the number of servers
			 * until the constraint is satisfied.
			 */
			if (q.length()>6) {
				openPosition=true;
			}else openPosition=false;

			// Get the index of the next available server
			int id = getFreeServer(serverFree);
			// BRANCH_1// Check if the condition to stop the simulation has been met.
			if (q.isEmpty() && clock >= endTime
					&& tnc == Double.POSITIVE_INFINITY) {
				percent = new DecimalFormat("0.0#%");
				System.out.println("Average waiting time: " + 60*ttwait
						/ ttServed+" minutes");
				System.out.println("Server free fraction: " + percent.format(ttServer/ ttServerFree));
				System.out.println("Maximum queue length: " + qLength);
				System.out.println("Total customers arrived: " + ta);
				System.out
						.println("Total customers served : " + (int) ttServed);
				System.out.println("Total customers >6min  : " + (int) ttWait6);
				System.out.println(" fraction              : "
						+ fraction);
				fractionAvg+=fraction;
				System.out.println(" server avg            : "
						+ ((double) busySrv/te));
				break;
			}

			// BRANCH_2// This is an arrival event and the end time is not reached
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

				// Sample for next arrival time so long that it doens't exceed endTime
				tna = clock + r.nextExponential(getLambda(clock));
				if (tna > tEnd) {
					tna = Double.POSITIVE_INFINITY;
				}

				ta++;
				// BRANCH_3_YES// Inner conditional loop to check if there is at
				// least one server free
				if (id != -1) {
					// Mark the server as busy by replacing the object in the arraylist
					Server tmpSrv0 = serverFree.get(id);
					tmpSrv0.setState(false);
					
					// Sample for time of next completion
					tmpSrv0.setTnc(clock + r.nextErlang(k, lambdaS));
					serverFree.set(id, tmpSrv0);
					
				} else {// BRANCH_3_NO//
					// Put person in the queue
					q.put(clock);
					// If there is need for servers add one here
					if (openPosition) serverFree.add(new Server(true));
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
					// BRANCH_5_YES// Inner conditional loop to check if the
					// queue is empty
					if (q.isEmpty()) {
						
						// If there is no more need; release the additional server
						if (!openPosition && serverFree.size()>nserv && id!=-1) serverFree.remove(id);
						
						// Get the first busy server and release them
						int busy = serverFree.indexOf(getNextTNC(serverFree));
						if (busy != -1){
							// Mark the server as busy by replacing the object in the arraylist
							Server tmpSrv1 = serverFree.get(busy);
							tmpSrv1.setState(true);
							serverFree.set(busy, tmpSrv1);
						}
						
						if (busy != -1){
							Server tmpSrv2 = serverFree.get(busy);
							tmpSrv2.setTnc(Double.POSITIVE_INFINITY);
							serverFree.set(busy, tmpSrv2);
						}
							

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
						Server tmpSrv3 = serverFree.get(tnc_index);
						tmpSrv3.setTnc(clock + r.nextErlang(k, lambdaS));
						serverFree.set(tnc_index, tmpSrv3);

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
	 * Method to get the appropriate average of arriving customers per hour of
	 * service. The distributions were provided by the manager. They can be 
	 * changed to any measured distribution.
	 * 
	 * @param Clock
	 *            The current time
	 * @return The average customers arriving for this hour
	 */
	private static double getLambda(double clock) {
		switch ((int) clock) {
		case 0:
			return 58.0;
		case 1:
			return 50.0;
		case 2:
			return 70.0;
		case 3:
			return 80.0;
		case 4:
			return 40.0;
		case 5:
			return 60.0;
		case 6:
			return 50.0;
		case 7:
			return 70.0;
		default:
			return 0;
		}

	}


	/**
	 * Find the first server that is free.
	 * @param serverFree The arraylist in which to look for a free server
	 * @return	The index of the position of the first free server
	 */
	private static int getFreeServer(ArrayList<Server> serverFree) {
		for (Server server : serverFree) {
			boolean state = server.isState();
			if (state){
				return serverFree.indexOf(server);
			}
		}return -1;
	}// End of getFreeServer method

	/**
	 * Find the first server that is busy.
	 * @param serverFree The arraylist in which to look for a busy server
	 * @return The index of the position of the first busy server
	 */
	private static int getBusyServer(ArrayList<Server> serverFree) {
		for (Server server : serverFree) {
			boolean state = server.isState();
			if (state){
				return serverFree.indexOf(server);
			}
		}return -1;
	}// End of getFreeServer method

	/**
	 * Count the free servers
	 * @param serverFree The arraylist in which to look for a free server
	 * @return The number of servers that are free
	 */
	private static int freeServersNo(ArrayList<Server> serverFree) {
		int servers = 0;
		for (Server server : serverFree) {
			boolean state = server.isState();
			if (state) servers++;
		}
		return servers;
	}// End of freeServersNo method
	
	/**
	 * Count the busy servers
	 * @param serverFree The arraylist in which to look for a busy server
	 * @return The number of servers that are busy
	 */
	private static int busyServersNo(ArrayList<Server> serverFree) {
		int servers = 0;
		for (Server server : serverFree) {
			boolean state = server.isState();
			if (!state) servers++;
		}
		return servers;
	}// End of busyServersNo method
	
	
	/**
	 * Find the server with the lowest completion time
	 * @param tnc_min The arraylist to search for completion times 
	 * @return The index of the lowest time of completion
	 */
	private static Server getNextTNC(ArrayList<Server> serverFree) {
		double tnc_min = Double.POSITIVE_INFINITY;
		Server index = null;
		for (Server server : serverFree) {
			if(server.getTnc() < tnc_min) {
				tnc_min = server.getTnc();
				index=server;
			}
			
		}
		return index;
	}// End of getNextTNC method
}// End of Class
