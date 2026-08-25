package service;

/*
  ProductService.java
  ProductService model class
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 26 March 2026
 */

import domain.Product;
import repository.ProductRepository;
import factory.ProductFactory;

import java.util.List;

public class ProductService implements IProductService {
    private final ProductRepository repository;

    public ProductService() {
        this.repository = new ProductRepository();
    }

    // Create and add a product
    @Override
    public Product addProduct(String id, String name, double price, int quantity, String category) {
        Product product = ProductFactory.createProduct(id, name, price, quantity, category);
        repository.addProduct(product);
        return product;
    }

    // Add a grocery product
    @Override
    public Product addGrocery(String id, String name, double price, int quantity) {
        Product product = ProductFactory.createGrocery(id, name, price, quantity);
        repository.addProduct(product);
        return product;
    }

    // Add a beverage product
    @Override
    public Product addBeverage(String id, String name, double price, int quantity) {
        Product product = ProductFactory.createBeverage(id, name, price, quantity);
        repository.addProduct(product);
        return product;
    }

    // Add a snack product
    @Override
    public Product addSnack(String id, String name, double price, int quantity) {
        Product product = ProductFactory.createSnack(id, name, price, quantity);
        repository.addProduct(product);
        return product;
    }

    // Add airtime product
    @Override
    public Product addAirtime(String id, String network, double amount) {
        Product product = ProductFactory.createAirtime(id, network, amount);
        repository.addProduct(product);
        return product;
    }

    // Get all products
    @Override
    public List<Product> getAllProducts() {
        return repository.getAllProducts();
    }

    // Find product by ID
    @Override
    public Product findProductById(String id) {
        return repository.findById(id);
    }

    // Find product by name
    @Override
    public Product findProductByName(String name) {
        return repository.findByName(name);
    }

    // Find products by category
    @Override
    public List<Product> findProductsByCategory(String category) {
        return repository.findByCategory(category);
    }

    // Update product by ID
    @Override
    public boolean updateProduct(String id, String name, double price, int quantity, String category) {
        Product existing = repository.findById(id);
        if (existing == null) {
            return false;
        }

        Product updatedProduct = new Product.Builder()
                .copy(existing)
                .setName(name)
                .setPrice(price)
                .setQuantity(quantity)
                .setCategory(category)
                .build();

        repository.updateProduct(id, updatedProduct);
        return true;
    }

    // Partial update - only update specified fields
    @Override
    public boolean updateProductFields(String id, String name, Double price, Integer quantity, String category) {
        Product existing = repository.findById(id);
        if (existing == null) {
            return false;
        }

        Product.Builder builder = new Product.Builder().copy(existing);

        if (name != null) {
            builder.setName(name);
        }
        if (price != null) {
            builder.setPrice(price);
        }
        if (quantity != null) {
            builder.setQuantity(quantity);
        }
        if (category != null) {
            builder.setCategory(category);
        }

        Product updatedProduct = builder.build();
        repository.updateProduct(id, updatedProduct);
        return true;
    }

    // Delete product by ID
    @Override
    public boolean deleteProduct(String id) {
        if (!repository.exists(id)) {
            return false;
        }
        repository.deleteProduct(id);
        return true;
    }

    // Delete product by name
    @Override
    public boolean deleteProductByName(String name) {
        if (!repository.existsByName(name)) {
            return false;
        }
        repository.deleteProductByName(name);
        return true;
    }

    // Check if product exists by ID
    @Override
    public boolean productExists(String id) {
        return repository.exists(id);
    }

    // Check if product exists by name
    @Override
    public boolean productExistsByName(String name) {
        return repository.existsByName(name);
    }

    // Get total number of products
    @Override
    public int getTotalProducts() {
        return repository.count();
    }

    // Get low stock products
    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return repository.getLowStock(threshold);
    }

    // Get products with stock below 10 (convenience method)
    @Override
    public List<Product> getLowStockProducts() {
        return repository.getLowStock(10);
    }

    // Update quantity only
    @Override
    public boolean updateQuantity(String id, int newQuantity) {
        Product existing = repository.findById(id);
        if (existing == null) {
            return false;
        }

        Product updatedProduct = new Product.Builder()
                .copy(existing)
                .setQuantity(newQuantity)
                .build();

        repository.updateProduct(id, updatedProduct);
        return true;
    }

    // Update price only
    @Override
    public boolean updatePrice(String id, double newPrice) {
        Product existing = repository.findById(id);
        if (existing == null) {
            return false;
        }

        Product updatedProduct = new Product.Builder()
                .copy(existing)
                .setPrice(newPrice)
                .build();

        repository.updateProduct(id, updatedProduct);
        return true;
    }
}