package za.ac.cput.adp_assignment02.service;

/*
 * OrderServiceTest.java
 * Tests the Point of Sale checkout.
 * Author: Rocco Given Visagie (220343527)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.dto.CheckoutRequest;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private IdGeneratorService idGenerator;
    @Mock private NotificationService notificationService;

    private OrderService service;
    private Customer customer;
    private Product bread;
    private Product cola;

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepository, orderItemRepository, productRepository, customerRepository,
                employeeRepository, idGenerator, notificationService);
        customer = new Customer.Builder().setCustID("CUS-0001").setFirstName("Sipho").setLastName("Ndlovu").build();
        bread = new Product.Builder().setId("PRD-0001").setName("White Bread").setPrice(15.99).setQuantity(50).build();
        cola = new Product.Builder().setId("PRD-0002").setName("Cola 2L").setPrice(24.50).setQuantity(3).build();
    }

    @Test
    void checkoutWorksOutTotalsReducesStockAndRecordsThePurchase() {
        Employee cashier = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();
        when(customerRepository.findById("CUS-0001")).thenReturn(Optional.of(customer));
        when(employeeRepository.findById("EMP-0002")).thenReturn(Optional.of(cashier));
        when(productRepository.findById("PRD-0001")).thenReturn(Optional.of(bread));
        when(productRepository.findById("PRD-0002")).thenReturn(Optional.of(cola));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(idGenerator.nextId(eq(IdGeneratorService.ORDER), any())).thenReturn("ORD-0001");
        when(idGenerator.nextId(eq(IdGeneratorService.ORDER_ITEM), any())).thenReturn("ITM-0001", "ITM-0002");

        CheckoutRequest request = new CheckoutRequest("CUS-0001", "EMP-0002", List.of(
                new CheckoutRequest.Line("PRD-0001", 2),
                new CheckoutRequest.Line("PRD-0002", 1),
                new CheckoutRequest.Line("PRD-0001", 1)));   // same product twice is combined

        Order order = service.checkout(request);

        assertEquals("ORD-0001", order.getOrderId());
        assertEquals("Sipho Ndlovu", order.getCustomerName());
        assertEquals("Lerato Dlamini", order.getEmployeeName());
        assertEquals(2, order.getOrderItems().size());
        assertEquals(3, order.getOrderItems().get(0).getQuantity());
        assertEquals(47.97, order.getOrderItems().get(0).getLineTotal(), 0.001);
        assertEquals(72.47, order.getSubtotal(), 0.001);
        assertEquals(10.87, order.getVatAmount(), 0.001);          // 15% VAT
        assertEquals(83.34, order.getTotalAmount(), 0.001);        // subtotal + VAT
        assertEquals(47, bread.getQuantity());
        assertEquals(2, cola.getQuantity());
        assertEquals(LocalDate.now(), customer.getLastPurchaseDate());
        verify(notificationService).checkStock(cola);
    }

    @Test
    void checkoutFailsWhenThereIsNotEnoughStock() {
        when(customerRepository.findById("CUS-0001")).thenReturn(Optional.of(customer));
        when(productRepository.findById("PRD-0002")).thenReturn(Optional.of(cola));
        when(idGenerator.nextId(eq(IdGeneratorService.ORDER), any())).thenReturn("ORD-0001");

        CheckoutRequest request = new CheckoutRequest("CUS-0001", null, List.of(new CheckoutRequest.Line("PRD-0002", 10)));

        assertThrows(IllegalStateException.class, () -> service.checkout(request));
        assertEquals(3, cola.getQuantity());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void checkoutNeedsACustomerAndItems() {
        assertThrows(IllegalArgumentException.class, () -> service.checkout(new CheckoutRequest("", null, List.of(new CheckoutRequest.Line("PRD-0001", 1)))));
        assertThrows(IllegalArgumentException.class, () -> service.checkout(new CheckoutRequest("CUS-0001", null, List.of())));
    }

    @Test
    void voidingAnOrderPutsTheStockBack() {
        Order order = new Order.Builder().setOrderId("ORD-0005").setCustomerId("CUS-0001")
                .addOrderItem(new za.ac.cput.adp_assignment02.domain.OrderItem.OrderItemBuilder()
                        .setOrderItemId("ITM-0009").setOrderId("ORD-0005").setProductId("PRD-0001").setQuantity(4).setLineTotal(63.96).build())
                .build();
        when(orderRepository.findById("ORD-0005")).thenReturn(Optional.of(order));
        when(productRepository.findById("PRD-0001")).thenReturn(Optional.of(bread));

        assertTrue(service.delete("ORD-0005"));
        assertEquals(54, bread.getQuantity());
        verify(orderRepository).delete(order);
    }
}
