package factory;

/*
  ProductFactory.java
  ProductFactory model class
  Author: Isaac Tinotenda Ziyengwa(231269307)
  Date: 26 March 2026
 */

import domain.Product;

public class ProductFactory {

    // Creating a product
    public static Product createProduct(String id, String name, double price, int quantity, String category) {
        return new Product.Builder()
                .setId(id)
                .setName(name)
                .setPrice(price)
                .setQuantity(quantity)
                .setCategory(category)
                .build();
    }

    // Creating a grocery product
    public static Product createGrocery(String id, String name, double price, int quantity) {
        return new Product.Builder()
                .setId(id)
                .setName(name)
                .setPrice(price)
                .setQuantity(quantity)
                .setCategory("Groceries")
                .build();
    }

    // Creating a beverage product
    public static Product createBeverage(String id, String name, double price, int quantity) {
        return new Product.Builder()
                .setId(id)
                .setName(name)
                .setPrice(price)
                .setQuantity(quantity)
                .setCategory("Beverages")
                .build();
    }

    // Creating a snack product
    public static Product createSnack(String id, String name, double price, int quantity) {
        return new Product.Builder()
                .setId(id)
                .setName(name)
                .setPrice(price)
                .setQuantity(quantity)
                .setCategory("Snacks")
                .build();
    }

    // Creating an airtime product
    public static Product createAirtime(String id, String network, double amount) {
        return new Product.Builder()
                .setId(id)
                .setName(network + " Airtime R" + amount)
                .setPrice(amount)
                .setQuantity(9999)
                .setCategory("Airtime")
                .build();
    }
}