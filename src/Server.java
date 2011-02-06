/**
 * 
 */

/**
 * @author Christos M. Delivorias
 *
 */
public class Server {
	
	private boolean state;
	private double tnc;
	
	/**
	 * @param state
	 */
	public Server(boolean state) {
		super();
		this.state = state;
		this.tnc=Double.POSITIVE_INFINITY;
	}
	/**
	 * 
	 * @return
	 */
	public boolean isState() {
		return state;
	}
	/**
	 * 
	 * @param state
	 */
	public void setState(boolean state) {
		this.state = state;
	}
	/**
	 * 
	 * @return
	 */
	public double getTnc() {
		return tnc;
	}
	/**
	 * 
	 * @param tnc
	 */
	public void setTnc(double tnc) {
		this.tnc = tnc;
	}

	
	
	

}
