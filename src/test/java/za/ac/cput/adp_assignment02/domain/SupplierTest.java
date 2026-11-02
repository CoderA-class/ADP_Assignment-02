package za.ac.cput.adp_assignment02.domain;

/*
 * SupplierTest.java
 * Tests for the Supplier domain class
 * Author: Luke John Zyster (220489114)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierTest {

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier.SupplierBuilder()
                .setSupplierId("SUP-0001")
                .setName("Cape Fresh Distributors")
                .setContactPerson("Thandi Mokoena")
                .setEmail("orders@capefresh.co.za")
                .setPhone("021 555 0101")
                .setAddress("12 Harbour Rd, Cape Town")
                .build();
    }

    @Test
    void builderSetsEveryField() {
        assertEquals("SUP-0001", supplier.getSupplierId());
        assertEquals("Cape Fresh Distributors", supplier.getName());
        assertEquals("Thandi Mokoena", supplier.getContactPerson());
        assertEquals("orders@capefresh.co.za", supplier.getEmail());
        assertEquals("021 555 0101", supplier.getPhone());
        assertEquals("12 Harbour Rd, Cape Town", supplier.getAddress());
        assertNull(supplier.getImageUrl());
    }

    @Test
    void settersUpdateValues() {
        supplier.setPhone("021 555 9999");
        supplier.setImageUrl("data:image/jpeg;base64,abc");
        assertEquals("021 555 9999", supplier.getPhone());
        assertEquals("data:image/jpeg;base64,abc", supplier.getImageUrl());
    }

    @Test
    void toStringContainsTheName() {
        assertTrue(supplier.toString().contains("Cape Fresh Distributors"));
    }

    // ---- team test (moved here from src/test/java/domain) ----

    @Test
    void builderCreatesSupplierWithAllFields() {
        Supplier supplier = new Supplier.SupplierBuilder().setSupplierId("S-1")
                .setName("Cape Produce").setEmail("orders@example.com")
                .setPhone("0211234567").setAddress("Cape Town").build();

        assertEquals("S-1", supplier.getSupplierId());
        assertEquals("Cape Produce", supplier.getName());
        assertEquals("orders@example.com", supplier.getEmail());
        assertEquals("0211234567", supplier.getPhone());
        assertEquals("Cape Town", supplier.getAddress());
    }
}
