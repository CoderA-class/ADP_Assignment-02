package za.ac.cput.adp_assignment02.domain;

/*
 * OrderTest.java
 * Tests for the Order domain class
 * Author: Rocco Given Visagie (220343527)
 */

import java.util.List;
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

    // ---- team test (moved here from src/test/java/domain) ----

    @Test
    void builderStoresOrderAndCalculatesItemTotal() {
    OrderItem first = new OrderItem.OrderItemBuilder()
        .setOrderItemId("OI-1").setOrderId("O-1").setProductId("P-1")
        .setQuantity(2).setLineTotal(30.0).build();
    OrderItem second = new OrderItem.OrderItemBuilder()
        .setOrderItemId("OI-2").setOrderId("O-1").setProductId("P-2")
        .setQuantity(1).setLineTotal(15.0).build();
    LocalDate date = LocalDate.of(2026, 6, 21);
    Order order = new Order.Builder().setOrderId("O-1").setCustomerId("C-1")
        .setOrderDate(date).setTotalAmount(45.0).setOrderItems(List.of(first, second)).build();

    assertEquals("O-1", order.getOrderId());
    assertEquals("C-1", order.getCustomerId());
    assertEquals(date, order.getOrderDate());
    assertEquals(45.0, order.getTotalAmount());
    assertEquals(2, order.getOrderItems().size());
    assertEquals(45.0, order.calculateTotal());
    }
}
