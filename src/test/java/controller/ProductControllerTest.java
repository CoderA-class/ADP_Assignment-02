package controller;

/*
  ProductControllerTest.java
  Test class for ProductController
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 19 July 2026
 */

import Domain.Product;
import service.IProductService;
import service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProductControllerTest {

    private ProductController controller;
    private IProductService service;

    @BeforeEach
    void setUp() {
        controller = new ProductController();
        service = new ProductService();
    }

    @Test
    void testCreateProduct() {
        // Create a product
        Product product = new Product.Builder()
                .setId("P001")
                .setName("Laptop")
                .setPrice(1500.00)
                .setQuantity(10)
                .setCategory("Electronics")
                .build();

        Product created = controller.createProduct(product);

        assertNotNull(created);
        assertEquals("P001", created.getId());
        assertEquals("Laptop", created.getName());
        assertEquals(1500.00, created.getPrice());
        assertEquals(10, created.getQuantity());
        assertEquals("Electronics", created.getCategory());
    }

    @Test
    void testGetAllProducts() {
        // Add some products first
        Product product1 = new Product.Builder()
                .setId("P001")
                .setName("Laptop")
                .setPrice(1500.00)
                .setQuantity(10)
                .setCategory("Electronics")
                .build();

        Product product2 = new Product.Builder()
                .setId("P002")
                .setName("Phone")
                .setPrice(800.00)
                .setQuantity(5)
                .setCategory("Electronics")
                .build();

        controller.createProduct(product1);
        controller.createProduct(product2);

        List<Product> products = controller.getAllProducts();

        assertNotNull(products);
        assertTrue(products.size() >= 2);
    }

    @Test
    void testGetProductById() {
        // Create a product
        Product product = new Product.Builder()
                .setId("P003")
                .setName("Tablet")
                .setPrice(400.00)
                .setQuantity(7)
                .setCategory("Electronics")
                .build();

        controller.createProduct(product);

        // Find by ID
        Product found = controller.getProductById("P003");

        assertNotNull(found);
        assertEquals("P003", found.getId());
        assertEquals("Tablet", found.getName());
    }

    @Test
    void testGetProductByIdNotFound() {
        Product found = controller.getProductById("NONEXISTENT");
        assertNull(found);
    }

    @Test
    void testUpdateProduct() {
        // Create a product
        Product product = new Product.Builder()
                .setId("P004")
                .setName("Headphones")
                .setPrice(100.00)
                .setQuantity(15)
                .setCategory("Accessories")
                .build();

        controller.createProduct(product);

        // Update the product
        Product updatedProduct = new Product.Builder()
                .setId("P004")
                .setName("Wireless Headphones")
                .setPrice(120.00)
                .setQuantity(20)
                .setCategory("Accessories")
                .build();

        boolean result = controller.updateProduct("P004", updatedProduct);

        assertTrue(result);

        // Verify the update
        Product found = controller.getProductById("P004");
        assertEquals("Wireless Headphones", found.getName());
        assertEquals(120.00, found.getPrice());
        assertEquals(20, found.getQuantity());
    }

    @Test
    void testUpdateProductNotFound() {
        Product product = new Product.Builder()
                .setId("P999")
                .setName("Nonexistent")
                .setPrice(0.00)
                .setQuantity(0)
                .setCategory("None")
                .build();

        boolean result = controller.updateProduct("P999", product);
        assertFalse(result);
    }

    @Test
    void testDeleteProduct() {
        // Create a product
        Product product = new Product.Builder()
                .setId("P005")
                .setName("Mouse")
                .setPrice(50.00)
                .setQuantity(25)
                .setCategory("Accessories")
                .build();

        controller.createProduct(product);

        // Delete the product
        boolean result = controller.deleteProduct("P005");
        assertTrue(result);

        // Verify it's deleted
        Product found = controller.getProductById("P005");
        assertNull(found);
    }

    @Test
    void testDeleteProductNotFound() {
        boolean result = controller.deleteProduct("NONEXISTENT");
        assertFalse(result);
    }
}