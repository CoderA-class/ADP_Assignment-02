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

    // ---- team test (moved here from src/test/java/factory) ----

    @Test
    void createsOrderItemAndRejectsInvalidQuantity() {
        OrderItem item = OrderItemFactory.createOrderItem("OI-1", "O-1", "P-1", 2, 39.98);

        assertEquals("OI-1", item.getOrderItemId());
        assertEquals("O-1", item.getOrderId());
        assertEquals("P-1", item.getProductId());
        assertEquals(2, item.getQuantity());
        assertEquals(39.98, item.getLineTotal());
        assertThrows(IllegalArgumentException.class,
                () -> OrderItemFactory.createOrderItem("OI-2", "O-1", "P-1", 0, 0));
    }
}
