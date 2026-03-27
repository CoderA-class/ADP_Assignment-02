package factory;

/*
  OrderFactory.java
  OrderFactory model class
  Author: Rocco Given Visagie (220343527)
  Date: 23 March 2026
 */

import domain.Order;

import java.time.LocalDate;
import java.util.UUID;

public class OrderFactory {

    public static Order createOrder(String customerId, LocalDate orderDate) {

        if (customerId == null || customerId.isEmpty()) {
            return null; // basic validation for TDD
        }

        String orderId = UUID.randomUUID().toString();

        return new Order.Builder()
                .setOrderId(orderId)
                .setCustomerId(customerId)
                .setOrderDate(orderDate)
                .setTotalAmount(0.0)
                .build();
    }
}