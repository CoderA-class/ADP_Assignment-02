package za.ac.cput.adp_assignment02.factory;

/*
 * OrderItemFactoryTest.java
 * Author: Adriaan van der Westhuizen (240436415)
 */

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.OrderItem;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemFactoryTest {

    @Test
    void createOrderItem() {
        OrderItem item = OrderItemFactory.createOrderItem("ITM-0001", "ORD-0001", "PRD-0001", 3, 47.97);
        assertEquals("ITM-0001", item.getOrderItemId());
        assertEquals("ORD-0001", item.getOrderId());
        assertEquals("PRD-0001", item.getProductId());
        assertEquals(3, item.getQuantity());
        assertEquals(47.97, item.getLineTotal());
    }

    @Test
    void invalidValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> OrderItemFactory.createOrderItem("", "ORD-0001", "PRD-0001", 1, 10));
        assertThrows(IllegalArgumentException.class, () -> OrderItemFactory.createOrderItem("ITM-0001", "", "PRD-0001", 1, 10));
        assertThrows(IllegalArgumentException.class, () -> OrderItemFactory.createOrderItem("ITM-0001", "ORD-0001", "", 1, 10));
        assertThrows(IllegalArgumentException.class, () -> OrderItemFactory.createOrderItem("ITM-0001", "ORD-0001", "PRD-0001", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> OrderItemFactory.createOrderItem("ITM-0001", "ORD-0001", "PRD-0001", 1, -5));
    }
}
