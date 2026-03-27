package factory;

/*
  OrderItemFactory.java
  OrderItemFactory model class
  Author: Adriaan van der Westhuizen (240436415)
  Date: 26 March 2026
 */

import domain.OrderItem;

public class OrderItemFactory {

    public static OrderItem createOrderItem(String orderItemId,
                                            String orderId,
                                            String productId,
                                            int quantity,
                                            double lineTotal) {

        if (orderItemId == null || orderItemId.isEmpty())
            throw new IllegalArgumentException("orderItemId is required");

        if (orderId == null || orderId.isEmpty())
            throw new IllegalArgumentException("orderId is required");

        if (productId == null || productId.isEmpty())
            throw new IllegalArgumentException("productId is required");

        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be greater than 0");

        if (lineTotal < 0)
            throw new IllegalArgumentException("Line total cannot be negative");

        return new OrderItem.OrderItemBuilder()
                .setOrderItemId(orderItemId)
                .setOrderId(orderId)
                .setProductId(productId)
                .setQuantity(quantity)
                .setLineTotal(lineTotal)
                .build();
    }
}
