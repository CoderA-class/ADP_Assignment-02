package za.ac.cput.adp_assignment02.factory;

/*
OrderFactory.java
Order Factory class
Author: Rocco Given Visagie (220343527)
Date: 19 August 2026
*/



import za.ac.cput.adp_assignment02.domain.Order;

import java.time.LocalDate;
import java.util.UUID;

public class OrderFactory {
    public static Order createOrder(String customerId, LocalDate orderDate) {
        if (customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (orderDate == null) {
            throw new IllegalArgumentException("orderDate is required");
        }
        String orderId = UUID.randomUUID().toString();
        return new Order.Builder().setOrderId(orderId).setCustomerId(customerId).setOrderDate(orderDate).setTotalAmount(0.0).build();
    }
}
