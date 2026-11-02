package za.ac.cput.adp_assignment02.domain;

/*
 * ProductTest.java
 * Tests for the Product domain class
 * Author: Isaac Tinotenda Ziyengwa (231269307)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product.Builder()
                .setId("PRD-0001")
                .setName("White Sugar 10kg")
                .setPrice(179.00)
                .setQuantity(25)
                .setCategory("Pantry")
                .setSupplierId("SUP-0001")
                .setReorderLevel(10)
                .build();
    }

    @Test
    void builderSetsEveryField() {
        assertEquals("PRD-0001", product.getId());
        assertEquals("White Sugar 10kg", product.getName());
        assertEquals(179.00, product.getPrice());
        assertEquals(25, product.getQuantity());
        assertEquals("Pantry", product.getCategory());
        assertEquals("SUP-0001", product.getSupplierId());
        assertEquals(10, product.getReorderLevel());
    }

    @Test
    void reorderLevelDefaultsToTen() {
        Product noReorder = new Product.Builder().setName("Rice").setQuantity(50).build();
        assertEquals(Product.DEFAULT_REORDER_LEVEL, noReorder.getReorderLevel());
    }

    @Test
    void isLowStockWhenAtOrBelowReorderLevel() {
        assertFalse(product.isLowStock());
        product.setQuantity(10);
        assertTrue(product.isLowStock());
        product.setQuantity(0);
        assertTrue(product.isLowStock());
    }

    @Test
    void copyKeepsAllValues() {
        Product copy = new Product.Builder().copy(product).setPrice(189.00).build();
        assertEquals("PRD-0001", copy.getId());
        assertEquals(189.00, copy.getPrice());
        assertEquals(25, copy.getQuantity());
    }

    @Test
    void settersUpdateValues() {
        product.setName("Brown Sugar 10kg");
        product.setCategory("Baking");
        assertEquals("Brown Sugar 10kg", product.getName());
        assertEquals("Baking", product.getCategory());
    }

    @Test
    void toStringContainsTheName() {
        assertTrue(product.toString().contains("White Sugar 10kg"));
    }

    // ---- team test (moved here from src/test/java/domain) ----

    @Test
    void builderCreatesProductWithAllFields() {
        Product product = new Product.Builder().setId("P-1").setName("Milk")
                .setPrice(18.50).setQuantity(12).setCategory("Dairy").build();

        assertEquals("P-1", product.getId());
        assertEquals("Milk", product.getName());
        assertEquals(18.50, product.getPrice());
        assertEquals(12, product.getQuantity());
        assertEquals("Dairy", product.getCategory());
    }
}
