/*
  SpazaShop Application
  ProductFactoryTest Class.java
  Student: Isaac Tinotenda Ziyengwa (231269307)
  Date: March 2026
 */

package factory;

import Domain.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    private Product product;
    private Product grocery;
    private Product beverage;
    private Product snack;
    private Product airtime;

    @BeforeEach
    void setUp() {
        // Initialize test products
        product = ProductFactory.createProduct("P001", "Bread", 15.99, 50, "Bakery");
        grocery = ProductFactory.createGrocery("G001", "Rice", 89.99, 30);
        beverage = ProductFactory.createBeverage("B001", "Coca-Cola", 12.50, 100);
        snack = ProductFactory.createSnack("S001", "Lays Chips", 8.99, 75);
        airtime = ProductFactory.createAirtime("A001", "Vodacom", 50.00);
    }

    @AfterEach
    void tearDown() {
        product = null;
        grocery = null;
        beverage = null;
        snack = null;
        airtime = null;
    }

    @Test
    void createProduct() {
        assertNotNull(product);
        assertEquals("P001", product.getId());
        assertEquals("Bread", product.getName());
        assertEquals(15.99, product.getPrice());
        assertEquals(50, product.getQuantity());
        assertEquals("Bakery", product.getCategory());
        System.out.println("Product created: " + product);
    }

    @Test
    void createGrocery() {
        assertNotNull(grocery);
        assertEquals("G001", grocery.getId());
        assertEquals("Rice", grocery.getName());
        assertEquals(89.99, grocery.getPrice());
        assertEquals(30, grocery.getQuantity());
        assertEquals("Groceries", grocery.getCategory());
        System.out.println("Grocery created: " + grocery);
    }

    @Test
    void createBeverage() {
        assertNotNull(beverage);
        assertEquals("B001", beverage.getId());
        assertEquals("Coca-Cola", beverage.getName());
        assertEquals(12.50, beverage.getPrice());
        assertEquals(100, beverage.getQuantity());
        assertEquals("Beverages", beverage.getCategory());
        System.out.println("Beverage created: " + beverage);
    }

    @Test
    void createSnack() {
        assertNotNull(snack);
        assertEquals("S001", snack.getId());
        assertEquals("Lays Chips", snack.getName());
        assertEquals(8.99, snack.getPrice());
        assertEquals(75, snack.getQuantity());
        assertEquals("Snacks", snack.getCategory());
        System.out.println("Snack created: " + snack);
    }

    @Test
    void createAirtime() {
        assertNotNull(airtime);
        assertEquals("A001", airtime.getId());
        assertEquals("Vodacom Airtime R50.0", airtime.getName());
        assertEquals(50.00, airtime.getPrice());
        assertEquals(9999, airtime.getQuantity());
        assertEquals("Airtime", airtime.getCategory());
        System.out.println("Airtime created: " + airtime);
    }



    @Test
    void testProductEquality() {
        Product product1 = ProductFactory.createProduct("P002", "Sugar", 25.00, 100, "Pantry");
        Product product2 = ProductFactory.createProduct("P002", "Sugar", 25.00, 100, "Pantry");


        assertEquals(product1.getId(), product2.getId());
        assertEquals(product1.getName(), product2.getName());
        assertEquals(product1.getPrice(), product2.getPrice());
        assertEquals(product1.getQuantity(), product2.getQuantity());
        assertEquals(product1.getCategory(), product2.getCategory());
    }
}