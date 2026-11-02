package za.ac.cput.adp_assignment02.domain;

/*
 * OrderTest.java
 * Tests for the Order domain class
 * Author: Rocco Given Visagie (220343527)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        OrderItem bread = new OrderItem.OrderItemBuilder().setOrderItemId("ITM-0001").setOrderId("ORD-0001")
                .setProductId("PRD-0001").setQuantity(2).setUnitPrice(156.00).setLineTotal(312.00).build();
        OrderItem cola = new OrderItem.OrderItemBuilder().setOrderItemId("ITM-0002").setOrderId("ORD-0001")
                .setProductId("PRD-0005").setQuantity(1).setUnitPrice(119.00).setLineTotal(119.00).build();
        order = new Order.Builder()
                .setOrderId("ORD-0001")
                .setCustomerId("CUS-0001")
                .setCustomerName("Sipho Ndlovu")
                .setOrderDate(LocalDate.of(2026, 11, 2))
                .addOrderItem(bread)
                .addOrderItem(cola)
                .build();
    }

    @Test
    void builderSetsEveryField() {
        assertEquals("ORD-0001", order.getOrderId());
        assertEquals("CUS-0001", order.getCustomerId());
        assertEquals("Sipho Ndlovu", order.getCustomerName());
        assertEquals(LocalDate.of(2026, 11, 2), order.getOrderDate());
        assertEquals(2, order.getOrderItems().size());
    }

    @Test
    void calculateTotalAddsUpLineTotals() {
        assertEquals(431.00, order.calculateTotal(), 0.001);
    }

    @Test
    void calculateTotalOfEmptyOrderIsZero() {
        Order empty = new Order.Builder().setOrderId("ORD-0002").setCustomerId("CUS-0001").build();
        assertEquals(0.0, empty.calculateTotal(), 0.001);
    }
}
