package za.ac.cput.adp_assignment02.factory;

/*
 * SupplierFactoryTest.java
 * Author: Luke John Zyster (220489114)
 */

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class SupplierFactoryTest {

    @Test
    void createSupplier() {
        Supplier s = SupplierFactory.createSupplier("SUP-0001", "Sunrise Beverages", "sales@sunrisebev.co.za", "021 555 0202", "8 Industria Way");
        assertNotNull(s);
        assertEquals("SUP-0001", s.getSupplierId());
        assertEquals("Sunrise Beverages", s.getName());
        assertEquals("sales@sunrisebev.co.za", s.getEmail());
        assertEquals("021 555 0202", s.getPhone());
        assertEquals("8 Industria Way", s.getAddress());
    }
}
