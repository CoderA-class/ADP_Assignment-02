package za.ac.cput.adp_assignment02.factory;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Order;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderFactoryTest {
    @Test
    void createsOrderWithGeneratedIdAndDate() {
        LocalDate date = LocalDate.of(2026, 6, 21);
        Order order = OrderFactory.createOrder("C-1", date);

        assertNotNull(order.getOrderId());
        assertEquals("C-1", order.getCustomerId());
        assertEquals(date, order.getOrderDate());
        assertEquals(0.0, order.getTotalAmount());
    }
}