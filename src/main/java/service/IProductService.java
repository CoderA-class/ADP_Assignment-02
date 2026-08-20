package service;

/*
  IProductService.java
  Product Service Interface
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 26 March 2026
 */

import domain.Product;
import java.util.List;

public interface IProductService {
    Product addProduct(String id, String name, double price, int quantity, String category);
    Product addGrocery(String id, String name, double price, int quantity);
    Product addBeverage(String id, String name, double price, int quantity);
    Product addSnack(String id, String name, double price, int quantity);
    Product addAirtime(String id, String network, double amount);
    List<Product> getAllProducts();
    Product findProductById(String id);
    Product findProductByName(String name);
    List<Product> findProductsByCategory(String category);
    boolean updateProduct(String id, String name, double price, int quantity, String category);
    boolean updateProductFields(String id, String name, Double price, Integer quantity, String category);
    boolean deleteProduct(String id);
    boolean deleteProductByName(String name);
    boolean productExists(String id);
    boolean productExistsByName(String name);
    int getTotalProducts();
    List<Product> getLowStockProducts(int threshold);
    List<Product> getLowStockProducts();
    boolean updateQuantity(String id, int newQuantity);
    boolean updatePrice(String id, double newPrice);
}