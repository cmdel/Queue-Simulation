import java.util.*;

/**
   This class extends java.util.Random by providing methods to sample
   from the exponential and Erlang distributions
   @author Andreas Grothey
   @version 17/10/03
*/

public class MyRandom extends Random{

    /**
       Constructor without seed: sequence of random numbers is always different
    */
    public MyRandom(){
	super();
    }
    
    /** 
	Constructor with seed: sequence of random numbers is always the same 
	(if the seed is the same)
	@param seed the seed to be used
    */
    public MyRandom(long seed){
	super(seed);
    }

    /** 
	Samples a random number from the exponential distribution with 
	parameter lambda. The expectation of the random number is 1/lambda.
	@param lambda parameter of exponential distribution (= 1/E(X)).
	@return an exponentially distributed random number
    */
    public double nextExponential(double lambda){
	double x = nextDouble();
	if (x<1e-16) return 1e+20;
	return -Math.log(x)/lambda;
    }

    /** 
	Samples a random number from the Erlang-k distribution with 
	parameter lambda. The expectation of the random number is 1/lambda. 
	@param k shape parameter
	@param lambda parameter of the Erlang distribution (=1/E(X)).
	@return an Erlang distributed random number
    */
    public double nextErlang(int k, double lambda){
	double mu; 
	double prod, x;

	mu = k*lambda;
	prod = 1.0;
	for(int i=0;i<k;i++)
	    prod *= nextDouble();

	if (prod<1e-16) prod = 1e-16;
	return -Math.log(prod)/mu;

    }
}
