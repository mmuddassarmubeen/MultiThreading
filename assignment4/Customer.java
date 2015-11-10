package p2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Customers are simulation actors that have two fields: a name, and a list
 * of Food items that constitute the Customer's order.  When running, an
 * customer attempts to enter the coffee shop (only successful if the
 * coffee shop has a free table), place its order, and then leave the 
 * coffee shop when the order is complete.
 */
public class Customer implements Runnable {
	//JUST ONE SET OF IDEAS ON HOW TO SET THINGS UP...
	private final String name;
	private final List<Food> order;
	private final int orderNum;
        private final Queue<Order> orderQueue;
        private final Lock lock;
        private final Condition notEmptyCondition;
	private static int runningCounter = 0;
        private final Semaphore customerLimit;

	/**
	 * You can feel free modify this constructor.  It must take at
	 * least the name and order but may take other parameters if you
	 * would find adding them useful.
	 */
	public Customer(String name, List<Food> order,Queue<Order> orderQueue
                , Lock lock, Condition notFullCondition, Condition notEmptyCondition
                ,Lock orderCompleteLock ,Condition orderCompleteCondition,Semaphore customerLimit ) {
		this.name = name;
		this.order = order;
		this.orderNum = ++runningCounter;
                this.orderQueue = orderQueue;
                this.lock = lock;
                this.notEmptyCondition = notEmptyCondition;
                this.customerLimit = customerLimit;
	}

	public String toString() {
		return name;
	}

	/** 
	 * This method defines what an Customer does: The customer attempts to
	 * enter the coffee shop (only successful when the coffee shop has a
	 * free table), place its order, and then leave the coffee shop
	 * when the order is complete.
	 */
	public void run() {
		//YOUR CODE GOES HERE...
            try{
                Order order = null;
                LinkedList<Food> ord = null;
                CountDownLatch orderLatch = new CountDownLatch(1);
                try{
                        this.customerLimit.acquire();
                        this.lock.lockInterruptibly();
                        try{
                            if(!Thread.currentThread().isInterrupted())
                            {
                                Simulation.logEvent(SimulationEvent.customerEnteredCoffeeShop(this));
                                ord = (LinkedList<Food>) this.order;
                                order = new Order(this.orderNum,ord,orderLatch);
                                System.out.println(this.name + " adds order:" + this.orderNum);
                                System.out.println("Order:"+ this.orderNum + " added to queue");
                                this.orderQueue.add(order);
                                notEmptyCondition.signalAll();
                                Simulation.logEvent(SimulationEvent.customerPlacedOrder(this, ord, orderNum));
                            }
                        }
                    catch(Exception ex)
                    {
                            System.out.println("General Customer exception while waiting for order" + ex.getMessage());
                            throw ex;
                    }
                    finally{
                        this.lock.unlock();
                    }
                
                    try
                    {
                        orderLatch.await();
                    }
                    catch(InterruptedException ex)
                    {
                        System.out.println("Customer Interrupted while waiting for order");
                        Thread.currentThread().interrupt();
                        throw ex;
                    }
                    catch(Exception ex)
                    {
                        System.out.println("General Customer exception while waiting for order" + ex.getMessage());
                        throw ex;
                    }
                    
                
                
            }
            catch(InterruptedException ex)
            {
                System.out.println("Customer Interrupted while waiting for order");
                Thread.currentThread().interrupt();
            }
            catch(Exception ex)
            {
                System.out.println("General Customer exception" + ex.getMessage());
            }
            finally
            {
                this.customerLimit.release();
                Simulation.logEvent(SimulationEvent.customerLeavingCoffeeShop(this));
            }
        }
        catch(Exception ex)
        {
            System.out.println("System exception");
        }
	}
}