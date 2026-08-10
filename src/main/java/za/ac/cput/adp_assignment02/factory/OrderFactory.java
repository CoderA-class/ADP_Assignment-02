package za.ac.cput.adp_assignment02.factory;

/*
OrderFactory.java
Order Factory class
Author: Rocco Given Visagie (220343527)
Date: 10 August 2026
*/



import za.ac.cput.adp_assignment02.domain.Order;

import java.time.LocalDate;
import java.util.UUID;

public class OrderFactory {
    public static Order createOrder(String customerId, LocalDate orderDate) {
        if (customerId == null || customerId.isEmpty()) {
            return null;
        }
        String orderId = UUID.randomUUID().toString();
        return new Order.Builder().setOrderId(orderId).setCustomerId(customerId).setOrderDate(orderDate).setTotalAmount(0.0).build();
    }
}
