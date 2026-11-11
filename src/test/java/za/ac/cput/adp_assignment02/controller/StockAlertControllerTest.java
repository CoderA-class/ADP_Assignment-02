package za.ac.cput.adp_assignment02.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.StockAlert;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.StockAlertService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * StockAlertControllerTest.java
 * Test class for StockAlertController – the alert is always raised as the logged-in employee.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class StockAlertControllerTest {

    @Mock private StockAlertService service;
    @Mock private CurrentUser currentUser;
    private StockAlertController controller;
    private final Employee lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();

    @BeforeEach
    void setUp() {
        controller = new StockAlertController(service, currentUser);
    }

    @Test
    void raisingAnAlertUsesTheLoggedInEmployee() {
        Product flour = new Product.Builder().setId("PRD-0010").setName("Cake Flour 10kg").setQuantity(4).setReorderLevel(10).build();
        StockAlert alert = new StockAlert(flour, lerato, "Shelf is empty");
        when(currentUser.require()).thenReturn(lerato);
        when(service.raise("PRD-0010", "Shelf is empty", lerato)).thenReturn(alert);

        StockAlert result = controller.raise(Map.of("productId", "PRD-0010", "note", "Shelf is empty"));

        assertEquals("EMP-0002", result.getRaisedById());
    }

    @Test
    void aProductMustBeChosen() {
        assertThrows(IllegalArgumentException.class, () -> controller.raise(Map.of("note", "low")));
        verifyNoInteractions(service);
    }
}
