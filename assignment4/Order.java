/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Muddassar
 */
public class Order {
    
    private final int orderId;
    private final List<Food> items;
    private final CountDownLatch orderLatch;
    
    public Order(int orderId, List<Food> items, CountDownLatch latch)
    {
        this.orderId = orderId;
        this.items = items;
        this.orderLatch = latch;
    }

    public int getOrderId() {
        return orderId;
    }

    public List<Food> getItems() {
        return items;
    }    

    public CountDownLatch getOrderLatch() {
        return orderLatch;
    }
}
