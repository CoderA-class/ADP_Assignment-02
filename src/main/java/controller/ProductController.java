package controller;

/*
  ProductController.java
  REST Controller for Product Management
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 19 July 2026
 */

import Domain.Product;
import service.IProductService;
import service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final IProductService productService;

    public ProductController() {
        this.productService = new ProductService();
    }

    // Create a product
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.addProduct(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory()
        );
    }

    // Get all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // Get product by ID
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productService.findProductById(id);
    }

    // Update product
    @PutMapping("/{id}")
    public boolean updateProduct(@PathVariable String id, @RequestBody Product product) {
        return productService.updateProduct(
                id,
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory()
        );
    }

    // Delete product
    @DeleteMapping("/{id}")
    public boolean deleteProduct(@PathVariable String id) {
        return productService.deleteProduct(id);
    }
}