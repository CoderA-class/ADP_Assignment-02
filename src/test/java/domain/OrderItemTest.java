package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderItemTest {
    @Test
    void builderStoresOrderItemFields() {
        OrderItem item = new OrderItem.OrderItemBuilder().setOrderItemId("OI-1")
                .setOrderId("O-1").setProductId("P-1").setQuantity(3)
                .setLineTotal(59.97).build();

        assertEquals("OI-1", item.getOrderItemId());
        assertEquals("O-1", item.getOrderId());
        assertEquals("P-1", item.getProductId());
        assertEquals(3, item.getQuantity());
        assertEquals(59.97, item.getLineTotal());
    }
}