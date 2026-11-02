package za.ac.cput.adp_assignment02.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Supplier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SupplierFactoryTest {
    @Test
    void createsSupplierAndRequiresAnId() {
        Supplier supplier = SupplierFactory.createSupplier(
                "S-1", "Cape Produce", "orders@example.com", "0211234567", "Cape Town");

        assertEquals("S-1", supplier.getSupplierId());
        assertEquals("Cape Produce", supplier.getName());
        assertEquals("orders@example.com", supplier.getEmail());
        assertEquals("0211234567", supplier.getPhone());
        assertEquals("Cape Town", supplier.getAddress());
        assertThrows(IllegalArgumentException.class,
                () -> SupplierFactory.createSupplier("", "Name", "a@b.com", "123", "Address"));
    }
}