package za.ac.cput.adp_assignment02.factory;

/*
 * OrderFactoryTest.java
 * Author: Rocco Given Visagie (220343527)
 */

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Order;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderFactoryTest {

    @Test
    void createOrder() {
        Order o = OrderFactory.createOrder("CUS-0001", LocalDate.of(2026, 11, 2));
        assertNotNull(o);
        assertNotNull(o.getOrderId());
        assertEquals("CUS-0001", o.getCustomerId());
        assertEquals(LocalDate.of(2026, 11, 2), o.getOrderDate());
        assertEquals(0.0, o.getTotalAmount());
        assertTrue(o.getOrderItems().isEmpty());
    }

    @Test
    void createOrderWithoutCustomerReturnsNull() {
        assertNull(OrderFactory.createOrder("", LocalDate.now()));
        assertNull(OrderFactory.createOrder(null, LocalDate.now()));
    }
}
