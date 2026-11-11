package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.repository.SupplierRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * SupplierServiceTest.java
 * Supplier IDs, validation and what happens to products when a supplier is removed.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock private SupplierRepository repository;
    @Mock private ProductRepository productRepository;
    @Mock private IdGeneratorService idGenerator;

    private SupplierService service;

    @BeforeEach
    void setUp() {
        service = new SupplierService(repository, productRepository, idGenerator);
    }

    @Test
    void createGeneratesAnIdAndTidiesTheDetails() {
        when(idGenerator.nextId(eq(IdGeneratorService.SUPPLIER), any())).thenReturn("SUP-0001");
        when(repository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        Supplier saved = service.create(new Supplier.SupplierBuilder().setName("  Cape Fresh Distributors ").setEmail(" orders@capefresh.co.za ").build());

        assertEquals("SUP-0001", saved.getSupplierId());
        assertEquals("Cape Fresh Distributors", saved.getName());
        assertEquals("orders@capefresh.co.za", saved.getEmail());
    }

    @Test
    void aNameIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new Supplier.SupplierBuilder().setName(" ").build()));
    }

    @Test
    void updatingAnUnknownSupplierIsRefused() {
        when(repository.existsById("SUP-0404")).thenReturn(false);
        assertThrows(NoSuchElementException.class, () -> service.update(new Supplier.SupplierBuilder().setSupplierId("SUP-0404").setName("X").build()));
    }

    @Test
    void deletingASupplierKeepsItsProductsButUnlinksThem() {
        Product sugar = new Product.Builder().setId("PRD-0001").setName("White Sugar 10kg").setSupplierId("SUP-0001").build();
        when(repository.existsById("SUP-0001")).thenReturn(true);
        when(productRepository.findBySupplierId("SUP-0001")).thenReturn(List.of(sugar));

        assertTrue(service.delete("SUP-0001"));
        assertNull(sugar.getSupplierId());
        verify(productRepository).save(sugar);
        verify(repository).deleteById("SUP-0001");
    }
}
