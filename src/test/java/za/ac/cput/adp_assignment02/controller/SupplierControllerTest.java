package za.ac.cput.adp_assignment02.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.service.SupplierService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * SupplierControllerTest.java
 * Test class for SupplierController
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class SupplierControllerTest {

    @Mock private SupplierService service;
    private SupplierController controller;
    private final Supplier capeFresh = new Supplier.SupplierBuilder().setSupplierId("SUP-0001").setName("Cape Fresh Distributors").build();

    @BeforeEach
    void setUp() {
        controller = new SupplierController(service);
    }

    @Test
    void createAndListDelegateToTheService() {
        when(service.create(capeFresh)).thenReturn(capeFresh);
        when(service.getAll()).thenReturn(List.of(capeFresh));
        assertEquals("SUP-0001", controller.create(capeFresh).getSupplierId());
        assertEquals(1, controller.getAll().size());
    }

    @Test
    void readReturns404WhenTheSupplierIsMissing() {
        when(service.read("SUP-0001")).thenReturn(capeFresh);
        when(service.read("SUP-0404")).thenReturn(null);
        assertEquals(HttpStatus.OK, controller.read("SUP-0001").getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.read("SUP-0404").getStatusCode());
    }

    @Test
    void deleteDelegatesToTheService() {
        when(service.delete("SUP-0001")).thenReturn(true);
        assertTrue(controller.delete("SUP-0001"));
    }
}
