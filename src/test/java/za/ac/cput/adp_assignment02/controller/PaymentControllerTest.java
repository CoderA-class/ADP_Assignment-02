package za.ac.cput.adp_assignment02.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.PaymentService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * PaymentControllerTest.java
 * Test class for PaymentController – full and part payments on account orders.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock private PaymentService service;
    @Mock private CurrentUser currentUser;
    private PaymentController controller;
    private final Employee cashier = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();

    @BeforeEach
    void setUp() {
        controller = new PaymentController(service, currentUser);
    }

    @Test
    void aPartPaymentPassesTheAmountThrough() {
        Order order = new Order.Builder().setOrderId("ORD-0010").build();
        when(currentUser.require()).thenReturn(cashier);
        when(service.recordPayment("ORD-0010", 150.0, "EFT", null, cashier)).thenReturn(order);

        assertSame(order, controller.pay("ORD-0010", Map.of("amount", "150", "method", "EFT")));
    }

    @Test
    void anEmptyAmountMeansPayItAllOff() {
        Order order = new Order.Builder().setOrderId("ORD-0010").build();
        Map<String, Object> body = new HashMap<>();
        body.put("amount", "");
        body.put("method", "CASH");
        when(currentUser.require()).thenReturn(cashier);
        when(service.recordPayment("ORD-0010", null, "CASH", null, cashier)).thenReturn(order);

        assertSame(order, controller.pay("ORD-0010", body));
    }

    @Test
    void anAmountThatIsNotANumberIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> controller.pay("ORD-0010", Map.of("amount", "lots")));
    }
}
