package za.ac.cput.adp_assignment02.factory;

/*
ProductFactory.java
Product Factory class
Author: Isaac Tinotenda Ziyengwa (231269307)
Date: 10 August 2026
*/


import za.ac.cput.adp_assignment02.domain.Product;

public class ProductFactory {
    public static Product createProduct(String id, String name, double price, int quantity, String category) {
        return new Product.Builder().setId(id).setName(name).setPrice(price).setQuantity(quantity).setCategory(category).build();
    }

    public static Product createGrocery(String id, String name, double price, int quantity) {
        return new Product.Builder().setId(id).setName(name).setPrice(price).setQuantity(quantity).setCategory("Groceries").build();
    }

    public static Product createBeverage(String id, String name, double price, int quantity) {
        return new Product.Builder().setId(id).setName(name).setPrice(price).setQuantity(quantity).setCategory("Beverages").build();
    }

    public static Product createSnack(String id, String name, double price, int quantity) {
        return new Product.Builder().setId(id).setName(name).setPrice(price).setQuantity(quantity).setCategory("Snacks").build();
    }

    public static Product createAirtime(String id, String network, double amount) {
        return new Product.Builder().setId(id).setName(network + " Airtime R" + amount).setPrice(amount).setQuantity(9999).setCategory("Airtime").build();
    }
}