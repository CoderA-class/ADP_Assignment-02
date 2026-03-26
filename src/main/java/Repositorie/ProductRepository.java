package Repositorie;

/*
        SpazaShop Application
        ProductRepository Class.java
        Student: Isaac Tinotenda Ziyengwa(231269307)
        Date: March 2026
        */


import Domain.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private List<Product> products; // Simple in-memory storage

    public ProductRepository() {
        this.products = new ArrayList<>();
    }


    // Adding a product
    public void addProduct(Product product) {
        products.add(product);
        System.out.println("Added: " + product.getName() + " (ID: " + product.getId() + ")");
    }

    // Get all the products
    public List<Product> getAllProducts() {
        return products;
    }

    // Finding the product by ID
    public Product findById(String id) {
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null; // Thats if it is Not found
    }


    public Product findByName(String name) {
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }
        return null;
    }

    // Finding products by category
    public List<Product> findByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategory().equalsIgnoreCase(category)) {
                result.add(p);
            }
        }
        return result;
    }

    // Updating a product
    public void updateProduct(String id, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equalsIgnoreCase(id)) {
                products.set(i, updatedProduct);
                System.out.println("Updated: " + updatedProduct.getName() + " (ID: " + id + ")");
                return;
            }
        }
        System.out.println("Product not found with ID: " + id);
    }

    // Overloaded method to update by name
    public void updateProductByName(String name, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getName().equalsIgnoreCase(name)) {
                products.set(i, updatedProduct);
                System.out.println("Updated: " + name);
                return;
            }
        }
        System.out.println("Product not found: " + name);
    }

    // Delete a product (now using ID)
    public void deleteProduct(String id) {
        Product toRemove = null;
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) {
                toRemove = p;
                break;
            }
        }
        if (toRemove != null) {
            products.remove(toRemove);
            System.out.println("Deleted: " + toRemove.getName() + " (ID: " + id + ")");
        } else {
            System.out.println("Product not found with ID: " + id);
        }
    }

    // Delete by name
    public void deleteProductByName(String name) {
        Product toRemove = null;
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(name)) {
                toRemove = p;
                break;
            }
        }
        if (toRemove != null) {
            products.remove(toRemove);
            System.out.println("Deleted: " + name);
        } else {
            System.out.println("Product not found: " + name);
        }
    }

    // Check if product exists by ID
    public boolean exists(String id) {
        for (Product p : products) {
            if (p.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    // Check if product exists by name
    public boolean existsByName(String name) {
        for (Product p : products) {
            if (p.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Getting total number of products
    public int count() {
        return products.size();
    }

    // Getting low stock products
    public List<Product> getLowStock(int threshold) {
        List<Product> lowStock = new ArrayList<>();
        for (Product p : products) {
            if (p.getQuantity() < threshold) {
                lowStock.add(p);
            }
        }
        return lowStock;
    }
}
