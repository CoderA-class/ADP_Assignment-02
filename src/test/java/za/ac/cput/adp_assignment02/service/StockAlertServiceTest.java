package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.StockAlert;
import za.ac.cput.adp_assignment02.event.StockLevelChangedEvent;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.repository.StockAlertRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * StockAlertServiceTest.java
 * Employee alerts the admins about low stock → admin acknowledges → restock closes it.
 */
@ExtendWith(MockitoExtension.class)
class StockAlertServiceTest {

    @Mock private StockAlertRepository repository;
    @Mock private ProductRepository productRepository;
    @Mock private NotificationService notificationService;

    private StockAlertService service;
    private Employee lerato;
    private Employee admin;
    private Product flour;

    @BeforeEach
    void setUp() {
        service = new StockAlertService(repository, productRepository, notificationService);
        lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();
        admin = new Employee.Builder().setEmpId("EMP-0001").setFName("Sam").setLName("Admin").build();
        flour = new Product.Builder().setId("PRD-0010").setName("Cake Flour 10kg").setPrice(149.99).setQuantity(4).setReorderLevel(10).build();
    }

    @Test
    void raisingAnAlertSavesItAndNotifiesTheAdmins() {
        when(productRepository.findById("PRD-0010")).thenReturn(Optional.of(flour));
        when(repository.findByProductIdAndStatusIn(eq("PRD-0010"), any())).thenReturn(List.of());
        when(repository.save(any(StockAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        StockAlert alert = service.raise("PRD-0010", "  Bakery customer wants 20 bags ", lerato);

        assertEquals(StockAlert.Status.OPEN, alert.getStatus());
        assertEquals("Lerato Dlamini", alert.getRaisedByName());
        assertEquals(4, alert.getQuantityAtReport());
        assertEquals("Bakery customer wants 20 bags", alert.getNote());
        verify(notificationService).notify(eq(Notification.Type.STOCK_ALERT), anyString(), anyString(), eq("PRD-0010"), eq(Notification.AUDIENCE_ADMIN));
    }

    @Test
    void theSameProductCannotBeReportedTwice() {
        when(productRepository.findById("PRD-0010")).thenReturn(Optional.of(flour));
        when(repository.findByProductIdAndStatusIn(eq("PRD-0010"), any())).thenReturn(List.of(new StockAlert(flour, lerato, null)));

        assertThrows(IllegalStateException.class, () -> service.raise("PRD-0010", null, admin));
        verify(repository, never()).save(any());
    }

    @Test
    void productsThatAreNotLowCannotBeReported() {
        Product rice = new Product.Builder().setId("PRD-0008").setName("Rice").setPrice(90).setQuantity(50).setReorderLevel(10).build();
        when(productRepository.findById("PRD-0008")).thenReturn(Optional.of(rice));

        assertThrows(IllegalArgumentException.class, () -> service.raise("PRD-0008", null, lerato));
    }

    @Test
    void acknowledgingTellsTheEmployee() {
        StockAlert open = new StockAlert(flour, lerato, null);
        when(repository.findById(1L)).thenReturn(Optional.of(open));
        when(repository.save(any(StockAlert.class))).thenAnswer(inv -> inv.getArgument(0));

        StockAlert saved = service.acknowledge(1L, admin);

        assertEquals(StockAlert.Status.ACKNOWLEDGED, saved.getStatus());
        assertEquals("Sam Admin", saved.getHandledByName());
        verify(notificationService).notify(eq(Notification.Type.STOCK_ALERT_UPDATE), anyString(), anyString(), eq("PRD-0010"), eq("EMP-0002"));
    }

    @Test
    void restockingClosesOpenAlertsAutomatically() {
        StockAlert open = new StockAlert(flour, lerato, null);
        when(repository.findByProductIdAndStatusIn(eq("PRD-0010"), any())).thenReturn(List.of(open));
        when(repository.save(any(StockAlert.class))).thenAnswer(inv -> inv.getArgument(0));
        flour.setQuantity(60);

        service.onStockLevelChanged(new StockLevelChangedEvent(flour));

        assertEquals(StockAlert.Status.RESOLVED, open.getStatus());
        verify(notificationService).markHandled(Notification.Type.STOCK_ALERT, "PRD-0010");
        verify(notificationService).notify(eq(Notification.Type.STOCK_ALERT_UPDATE), anyString(), anyString(), eq("PRD-0010"), eq("EMP-0002"));
    }

    @Test
    void stillLowStockDoesNotCloseAlerts() {
        service.onStockLevelChanged(new StockLevelChangedEvent(flour));
        verify(repository, never()).findByProductIdAndStatusIn(any(), any());
        verify(repository, never()).save(any());
    }
}
