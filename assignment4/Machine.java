package p2;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A Machine is used to make a particular Food.  Each Machine makes
 * just one kind of Food.  Each machine has a capacity: it can make
 * that many food items in parallel; if the machine is asked to
 * produce a food item beyond its capacity, the requester blocks.
 * Each food item takes at least item.cookTimeMS milliseconds to
 * produce.
 */
public class Machine {
	public final String machineName;
	public final Food machineFoodType;
        
	//YOUR CODE GOES HERE...
        private final ExecutorService executor;

	/**
	 * The constructor takes at least the name of the machine,
	 * the Food item it makes, and its capacity.  You may extend
	 * it with other arguments, if you wish.  Notice that the
	 * constructor currently does nothing with the capacity; you
	 * must add code to make use of this field (and do whatever
	 * initialization etc. you need).
	 */
	public Machine(String nameIn, Food foodIn, int capacityIn) {
		this.machineName = nameIn;
		this.machineFoodType = foodIn;
		
		//YOUR CODE GOES HERE...
                executor = Executors.newFixedThreadPool(capacityIn);
	}
	

	

	/**
	 * This method is called by a Cook in order to make the Machine's
	 * food item.  You can extend this method however you like, e.g.,
	 * you can have it take extra parameters or return something other
	 * than Object.  It should block if the machine is currently at full
	 * capacity.  If not, the method should return, so the Cook making
	 * the call can proceed.  You will need to implement some means to
	 * notify the calling Cook when the food item is finished.
	 */
	public Future makeFood(int orderNumber) throws InterruptedException {
		//YOUR CODE GOES HERE...
            System.out.println(this.machineName + "servicing food for order:" + orderNumber);
            //Simulation.logEvent(SimulationEvent.machineCookingFood(this, machineFoodType));
            return executor.submit(new CookAnItem(orderNumber));
	}

	//THIS MIGHT BE A USEFUL METHOD TO HAVE AND USE BUT IS JUST ONE IDEA
	private class CookAnItem implements Runnable {
            private final int orderNumber;
            public CookAnItem(int orderNumber)
            {
                this.orderNumber = orderNumber;
            }
		public void run() {
			try {
				//YOUR CODE GOES HERE...
                            Thread.sleep(machineFoodType.cookTimeMS);
                            Simulation.logEvent(SimulationEvent.machineDoneFood(Machine.this, machineFoodType));
                            System.out.println("Machine cooked:" + machineFoodType.toString() + "for order:"+ orderNumber);
			} 
                    catch(InterruptedException e) { 
                        executor.shutdown();
                        System.out.println("Machine Interrupted");
                        Thread.currentThread().interrupt();
                    }
		}
	}
 

	public String toString() {
		return machineName;
	}
        
        public void shutdownMachine()
        {
            this.executor.shutdown();
            Simulation.logEvent(SimulationEvent.machineEnding(this));
        }
}