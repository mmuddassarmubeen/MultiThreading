package p2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Cooks are simulation actors that have at least one field, a name.
 * When running, a cook attempts to retrieve outstanding orders placed
 * by Eaters and process them.
 */
public class Cook implements Runnable {
	private final String name;
        private final Queue<Order> orderQueue;
        private final Lock lock;
        private final Condition notEmptyCondition;
        private final Lock orderCompleteLock;
        private final Machine grill;
        private final Machine fryer;
        private final Machine coffeeMaker;
        Future grillTask = null;
        Future fryerTask = null;
        Future coffeeMakerTask = null;
	/**
	 * You can feel free modify this constructor.  It must
	 * take at least the name, but may take other parameters
	 * if you would find adding them useful. 
	 *
	 * @param: the name of the cook
	 */
	public Cook(String name, Queue<Order> orderQueue
                , Lock lock, Condition notEmptyCondition
                ,Lock orderComleteLock, Machine grill, Machine fryer, Machine coffeeMaker) {
		this.name = name;
                this.orderQueue = orderQueue;
                this.lock = lock;
                this.notEmptyCondition = notEmptyCondition;
                this.orderCompleteLock = orderComleteLock;
                this.grill = grill;
                this.fryer = fryer;
                this.coffeeMaker = coffeeMaker;
	}

	public String toString() {
		return name;
	}

	/**
	 * This method executes as follows.  The cook tries to retrieve
	 * orders placed by Customers.  For each order, a List<Food>, the
	 * cook submits each Food item in the List to an appropriate
	 * Machine, by calling makeFood().  Once all machines have
	 * produced the desired Food, the order is complete, and the Customer
	 * is notified.  The cook can then go to process the next order.
	 * If during its execution the cook is interrupted (i.e., some
	 * other thread calls the interrupt() method on it, which could
	 * raise InterruptedException if the cook is blocking), then it
	 * terminates.
	 */
	public void run() {

            Simulation.logEvent(SimulationEvent.cookStarting(this));
                
            try {
                    //YOUR CODE GOES HERE...
                    while(true)
                    {
                        
                        if(this.orderQueue.size() !=0)
                        {
                        
                        }
                        Order ord = null;
                        this.lock.lockInterruptibly();
                        try
                        {
                            while(orderQueue.size() == 0)
                            {
                                notEmptyCondition.await();
                            }
                            if(!Thread.currentThread().isInterrupted())
                            {
                                ord = orderQueue.poll();
                                System.out.println(this.name + " got the order:" + ord.getOrderId());
                            }
                        }
                        catch(InterruptedException ex)
                        {
                            System.out.println("Cook interrupted while picking order");
                        }
                        finally
                        {
                            this.lock.unlock();
                            if(Thread.currentThread().isInterrupted())
                            {
                                break;
                            }
                        }

                        this.orderCompleteLock.lock();
                        try
                        {
                            List<Food> order = ord.getItems();

                            for(Food food : order)
                            {
                                if(grill.machineFoodType.name.equals(food.toString()))
                                {
                                    grillTask = grill.makeFood(ord.getOrderId());
                                }
                                else if(fryer.machineFoodType.name.equals(food.toString()))
                                {
                                    fryerTask = fryer.makeFood(ord.getOrderId());
                                }
                                else if(coffeeMaker.machineFoodType.name.equals(food.toString()))
                                {
                                    coffeeMakerTask = coffeeMaker.makeFood(ord.getOrderId());
                                }
                            }
                            
                            grillTask.get();
                            fryerTask.get();
                            coffeeMakerTask.get();
                            
                            if(grillTask.isDone() && fryerTask.isDone() && coffeeMakerTask.isDone())
                            {
                                ord.getOrderLatch().countDown();
                                Simulation.logEvent(SimulationEvent.cookCompletedOrder(this, ord.getOrderId()));
                            }
                        }
                        catch(InterruptedException ex)
                        {
                            Thread.currentThread().interrupt();
                            System.out.println("Exception in Cook order completed");
                        }
                        finally
                        {
                            this.orderCompleteLock.unlock();
                            if(Thread.currentThread().isInterrupted())
                            {
                                break;
                            }
                        }
                    }
                }
                catch(InterruptedException e) {
                        // This code assumes the provided code in the Simulation class
                        // that interrupts each cook thread when all customers are done.
                        // You might need to change this if you change how things are
                        // done in the Simulation class.
                        System.out.println(Thread.currentThread().getName() +"cook interrupted");
                        grillTask.cancel(true);
                        fryerTask.cancel(true);
                        coffeeMakerTask.cancel(true);
                        Thread.currentThread().interrupt();
                         
                }
                catch(Exception e)
                {
                   System.out.println("Exception in cook" + e.getMessage());   
                }
                finally
                {
                    Simulation.logEvent(SimulationEvent.cookEnding(this));
                }
                
	}
}