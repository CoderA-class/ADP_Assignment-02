package za.ac.cput.adp_assignment02.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemFactoryTest {
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