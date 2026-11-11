package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.Payment;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.PaymentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * PaymentServiceTest.java
 * Sales on account: part payments, paying off the balance and the limits.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository repository;
    @Mock private OrderRepository orderRepository;
    @Mock private NotificationService notificationService;

    private PaymentService service;
    private Order onAccount;
    private final Employee cashier = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository, orderRepository, notificationService);
        onAccount = new Order.Builder().setOrderId("ORD-0010").setCustomerId("CUS-0001").setCustomerName("Sipho Ndlovu").build();
        onAccount.applyVat(1000.00, 0.15);          // R 1 150.00 incl. VAT
        onAccount.setPaymentMethod("ACCOUNT");
        onAccount.setAmountPaid(0.0);
    }

    @Test
    void anAccountSaleStartsUnpaid() {
        assertEquals(1150.00, onAccount.getBalanceDue(), 0.001);
        assertEquals("UNPAID", onAccount.getPaymentStatus());
    }

    @Test
    void aPartPaymentLowersTheBalance() {
        when(orderRepository.findById("ORD-0010")).thenReturn(Optional.of(onAccount));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = service.recordPayment("ORD-0010", 400.0, "CASH", null, cashier);

        assertEquals(750.00, saved.getBalanceDue(), 0.001);
        assertEquals("PARTIAL", saved.getPaymentStatus());
        verify(repository).save(any(Payment.class));
    }

    @Test
    void leavingTheAmountEmptyPaysOffTheWholeBalance() {
        when(orderRepository.findById("ORD-0010")).thenReturn(Optional.of(onAccount));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = service.recordPayment("ORD-0010", null, "EFT", null, cashier);

        assertEquals(0.0, saved.getBalanceDue(), 0.001);
        assertEquals("PAID", saved.getPaymentStatus());
    }

    @Test
    void youCannotPayMoreThanIsOwedOrPayAPaidOrder() {
        when(orderRepository.findById("ORD-0010")).thenReturn(Optional.of(onAccount));
        assertThrows(IllegalArgumentException.class, () -> service.recordPayment("ORD-0010", 2000.0, "CASH", null, cashier));
        assertThrows(IllegalArgumentException.class, () -> service.recordPayment("ORD-0010", 100.0, "ACCOUNT", null, cashier));

        onAccount.setAmountPaid(1150.0);
        assertThrows(IllegalStateException.class, () -> service.recordPayment("ORD-0010", 10.0, "CASH", null, cashier));
    }

    @Test
    void balancesAddUpPerCustomer() {
        Order second = new Order.Builder().setOrderId("ORD-0011").setCustomerId("CUS-0001").build();
        second.applyVat(100.00, 0.15);
        second.setPaymentMethod("ACCOUNT");
        second.setAmountPaid(15.0);
        Order cash = new Order.Builder().setOrderId("ORD-0012").setCustomerId("CUS-0002").build();
        cash.applyVat(50.00, 0.15);
        cash.setPaymentMethod("CASH");
        when(orderRepository.findAll()).thenReturn(List.of(onAccount, second, cash));

        Map<String, Double> balances = service.balances();

        assertEquals(1250.00, balances.get("CUS-0001"), 0.001);
        assertFalse(balances.containsKey("CUS-0002"));
    }
}
