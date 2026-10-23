package za.ac.cput.adp_assignment02.repository;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.persistence.EntityManager;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.domain.Product;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class OrderItemRepositoryTest {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndLoadsOrderRelationships() {
    Customer customer = new Customer.Builder().setCustID("C-1").setFirstName("Ava")
        .setLastName("Mokoena").setEmail("ava@example.com")
        .setPhoneNumber("0821234567").setAddress("Cape Town").build();
    Product product = new Product.Builder().setId("P-1").setName("Milk")
        .setPrice(18.50).setQuantity(12).setCategory("Dairy").build();
    customerRepository.save(customer);
    productRepository.save(product);

    Order order = new Order.Builder().setOrderId("O-1").setCustomerId("C-1")
        .setOrderDate(LocalDate.of(2026, 6, 21)).setTotalAmount(37.0).build();
    orderRepository.save(order);
    orderItemRepository.saveAndFlush(new OrderItem.OrderItemBuilder()
        .setOrderItemId("OI-1").setOrderId("O-1").setProductId("P-1")
        .setQuantity(2).setLineTotal(37.0).build());

    entityManager.clear();
    Order loadedOrder = orderRepository.findById("O-1").orElseThrow();
    assertEquals(1, loadedOrder.getOrderItems().size());
    assertEquals("P-1", loadedOrder.getOrderItems().get(0).getProductId());
    }

    @Test
    void rejectsOrderWithUnknownCustomer() {
    Order order = new Order.Builder().setOrderId("O-404").setCustomerId("C-404")
        .setOrderDate(LocalDate.of(2026, 6, 21)).setTotalAmount(0).build();

    assertThrows(DataIntegrityViolationException.class, () -> orderRepository.saveAndFlush(order));
    }
}