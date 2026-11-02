package za.ac.cput.adp_assignment02.domain;

/*
 * OrderItemTest.java
 * Tests for the OrderItem domain class
 * Author: Adriaan van der Westhuizen (240436415)
 */

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void builderSetsEveryField() {
        OrderItem item = new OrderItem.OrderItemBuilder()
                .setOrderItemId("ITM-0001")
                .setOrderId("ORD-0001")
                .setProductId("PRD-0001")
                .setProductName("White Bread 700g")
                .setUnitPrice(15.99)
                .setQuantity(3)
                .setLineTotal(47.97)
                .build();
        assertEquals("ITM-0001", item.getOrderItemId());
        assertEquals("ORD-0001", item.getOrderId());
        assertEquals("PRD-0001", item.getProductId());
        assertEquals("White Bread 700g", item.getProductName());
        assertEquals(15.99, item.getUnitPrice(), 0.001);
        assertEquals(3, item.getQuantity());
        assertEquals(47.97, item.getLineTotal());
    }

    @Test
    void toStringContainsTheIds() {
        OrderItem item = new OrderItem.OrderItemBuilder().setOrderItemId("ITM-0009").setOrderId("ORD-0003").build();
        assertTrue(item.toString().contains("ITM-0009"));
        assertTrue(item.toString().contains("ORD-0003"));
    }
}
