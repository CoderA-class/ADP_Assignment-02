package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SupplierTest {
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