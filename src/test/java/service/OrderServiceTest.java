package za.ac.cput.adp_assignment02.service;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class OrderServiceTest {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;

    private OrderService orderService;
    private OrderItemService orderItemService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, customerRepository);
        orderItemService = new OrderItemService(orderItemRepository, orderRepository, productRepository);
    }

    @Test
    void rejectsOrderForUnknownCustomerAsBadRequest() {
        Order order = new Order.Builder().setOrderId("O-1").setCustomerId("missing")
                .setOrderDate(LocalDate.of(2026, 6, 21)).setTotalAmount(0).build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.create(order));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Customer does not exist: missing", exception.getReason());
    }

    @Test
    void rejectsOrderItemWithUnknownOrderOrProductAsBadRequest() {
        OrderItem unknownOrder = orderItem("missing", "P-1");
        ResponseStatusException orderException = assertThrows(ResponseStatusException.class,
                () -> orderItemService.create(unknownOrder));
        assertEquals(HttpStatus.BAD_REQUEST, orderException.getStatusCode());
        assertEquals("Order does not exist: missing", orderException.getReason());

        customerRepository.save(customer("C-1"));
        orderRepository.save(order("O-1", "C-1"));
        OrderItem unknownProduct = orderItem("O-1", "missing");
        ResponseStatusException productException = assertThrows(ResponseStatusException.class,
                () -> orderItemService.create(unknownProduct));
        assertEquals(HttpStatus.BAD_REQUEST, productException.getStatusCode());
        assertEquals("Product does not exist: missing", productException.getReason());
    }

    private Customer customer(String id) {
        return new Customer.Builder().setCustID(id).setFirstName("Ava").setLastName("Mokoena")
                .setEmail("ava@example.com").setPhoneNumber("0821234567").setAddress("Cape Town").build();
    }

    private Order order(String id, String customerId) {
        return new Order.Builder().setOrderId(id).setCustomerId(customerId)
                .setOrderDate(LocalDate.of(2026, 6, 21)).setTotalAmount(0).build();
    }

    private OrderItem orderItem(String orderId, String productId) {
        return new OrderItem.OrderItemBuilder().setOrderItemId("OI-1").setOrderId(orderId)
                .setProductId(productId).setQuantity(1).setLineTotal(1).build();
    }
}
