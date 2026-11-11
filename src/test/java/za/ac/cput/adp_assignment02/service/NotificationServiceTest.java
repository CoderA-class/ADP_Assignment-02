package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.NotificationRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

/*
 * NotificationServiceTest.java
 * Low-stock and inactive-customer checks, and who gets to see which notifications.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repository;
    @Mock private ProductRepository productRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ApplicationEventPublisher events;

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(repository, productRepository, customerRepository, 30, events);
    }

    @Test
    void lowAndOutOfStockProductsRaiseOneNotificationEach() {
        Product flour = new Product.Builder().setId("PRD-0010").setName("Cake Flour 10kg").setQuantity(4).setReorderLevel(10).build();
        Product juice = new Product.Builder().setId("PRD-0006").setName("Orange Juice 1L").setQuantity(0).setReorderLevel(10).build();
        when(repository.existsByTypeAndRefIdAndSeenFalse(any(), any())).thenReturn(false);

        service.checkStock(flour);
        service.checkStock(juice);

        verify(repository, times(2)).save(any(Notification.class));
    }

    @Test
    void anUnreadAlertIsNotRepeated() {
        Product flour = new Product.Builder().setId("PRD-0010").setName("Cake Flour 10kg").setQuantity(4).setReorderLevel(10).build();
        when(repository.existsByTypeAndRefIdAndSeenFalse(Notification.Type.LOW_STOCK, "PRD-0010")).thenReturn(true);

        service.checkStock(flour);

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void adminsAlsoSeeAdminNotifications() {
        when(repository.findForAudiences(anyCollection())).thenReturn(List.of());

        service.getFor("EMP-0001", Employee.AccessLevel.ADMIN);

        verify(repository).findForAudiences(argThatContains(Notification.AUDIENCE_ADMIN));
    }

    @Test
    void customersWhoHaveNotBoughtFor30DaysAreFlagged() {
        Customer quiet = new Customer.Builder().setCustID("CUS-0003").setFirstName("Johan").setLastName("van Wyk").build();
        quiet.setLastPurchaseDate(LocalDate.now().minusDays(45));
        when(productRepository.findAll()).thenReturn(List.of());
        when(customerRepository.findAll()).thenReturn(List.of(quiet));
        when(repository.existsByTypeAndRefIdAndSeenFalse(Notification.Type.INACTIVE_CUSTOMER, "CUS-0003")).thenReturn(false);

        service.runAutomaticChecks();

        verify(repository).save(any(Notification.class));
    }

    private static java.util.Collection<String> argThatContains(String value) {
        return org.mockito.ArgumentMatchers.argThat(c -> c != null && c.contains(value));
    }
}
