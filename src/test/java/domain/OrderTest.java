package za.ac.cput.adp_assignment02.domain;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTest {
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