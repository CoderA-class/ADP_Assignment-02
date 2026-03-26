package factory;

/*
  SpazaShop Application
  ProductFactory Class.java
  Student: Isaac Tinotenda Ziyengwa(231269307)
  Date: March 2026
 */

import Domain.Product;

public class ProductFactory {

    // Creating a product
    public static Product createProduct(String id, String name, double price, int quantity, String category) {
        return new Product(id, name, price, quantity, category);
    }

    // Creating a grocery product
    public static Product createGrocery(String id, String name, double price, int quantity) {
        return new Product(id, name, price, quantity, "Groceries");
    }

    // Creating a beverage product
    public static Product createBeverage(String id, String name, double price, int quantity) {
        return new Product(id, name, price, quantity, "Beverages");
    }

    // Creating a snack product
    public static Product createSnack(String id, String name, double price, int quantity) {
        return new Product(id, name, price, quantity, "Snacks");
    }

    // Creating an airtime product
    public static Product createAirtime(String id, String network, double amount) {
        String name = network + " Airtime R" + amount;
        return new Product(id, name, amount, 9999, "Airtime");
    }
}