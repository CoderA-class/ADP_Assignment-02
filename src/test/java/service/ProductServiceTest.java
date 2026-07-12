package service;

/*
  ProductServiceTest.java
  Test class for ProductService
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 26 March 2026
 */

import Domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService();
        service.addProduct("P001", "Milk", 15.99, 50, "Dairy");
        service.addGrocery("P002", "Bread", 12.50, 30);
        service.addBeverage("P003", "Coke", 18.00, 100);
        service.addSnack("P004", "Chips", 8.50, 25);
        service.addAirtime("P005", "MTN", 50.00);
    }

    @Test
    void testAddProduct() {
        Product p = service.addProduct("P006", "Cheese", 45.99, 15, "Dairy");
        assertNotNull(p);
        assertEquals("P006", p.getId());
        assertEquals(6, service.getTotalProducts());
    }

    @Test
    void testFindById() {
        Product p = service.findProductById("P001");
        assertNotNull(p);
        assertEquals("Milk", p.getName());
        assertNull(service.findProductById("P999"));
    }

    @Test
    void testFindByName() {
        Product p = service.findProductByName("Bread");
        assertNotNull(p);
        assertEquals("P002", p.getId());
        assertNull(service.findProductByName("Nonexistent"));
    }

    @Test
    void testFindByCategory() {
        List<Product> products = service.findProductsByCategory("Airtime");
        assertEquals(1, products.size());
        assertEquals("MTN Airtime R50.0", products.get(0).getName());

        List<Product> empty = service.findProductsByCategory("Electronics");
        assertTrue(empty.isEmpty());
    }

    @Test
    void testUpdateProduct() {
        boolean updated = service.updateProduct("P001", "Fresh Milk", 18.99, 45, "Dairy");
        assertTrue(updated);

        Product p = service.findProductById("P001");
        assertEquals("Fresh Milk", p.getName());
        assertEquals(18.99, p.getPrice());

        assertFalse(service.updateProduct("P999", "Test", 10.00, 10, "Test"));
    }

    @Test
    void testUpdateProductFields() {
        // Update only price
        boolean updated = service.updateProductFields("P002", null, 14.99, null, null);
        assertTrue(updated);
        assertEquals(14.99, service.findProductById("P002").getPrice());

        // Update only quantity
        updated = service.updateProductFields("P002", null, null, 50, null);
        assertTrue(updated);
        assertEquals(50, service.findProductById("P002").getQuantity());
    }

    @Test
    void testDeleteProduct() {
        assertTrue(service.deleteProduct("P003"));
        assertNull(service.findProductById("P003"));
        assertEquals(4, service.getTotalProducts());
        assertFalse(service.deleteProduct("P999"));
    }

    @Test
    void testDeleteByName() {
        assertTrue(service.deleteProductByName("Chips"));
        assertNull(service.findProductByName("Chips"));
        assertEquals(4, service.getTotalProducts());
        assertFalse(service.deleteProductByName("Nonexistent"));
    }

    @Test
    void testProductExists() {
        assertTrue(service.productExists("P001"));
        assertFalse(service.productExists("P999"));
        assertTrue(service.productExistsByName("Milk"));
        assertFalse(service.productExistsByName("Nonexistent"));
    }

    @Test
    void testLowStock() {
        service.addProduct("P006", "Butter", 25.00, 5, "Dairy");
        List<Product> lowStock = service.getLowStockProducts(10);
        assertEquals(1, lowStock.size());
        assertEquals(5, lowStock.get(0).getQuantity());
    }

    @Test
    void testUpdateQuantityAndPrice() {
        assertTrue(service.updateQuantity("P004", 100));
        assertEquals(100, service.findProductById("P004").getQuantity());

        assertTrue(service.updatePrice("P005", 55.00));
        assertEquals(55.00, service.findProductById("P005").getPrice());

        assertFalse(service.updateQuantity("P999", 100));
        assertFalse(service.updatePrice("P999", 55.00));
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = service.getAllProducts();
        assertEquals(5, products.size());
    }
}