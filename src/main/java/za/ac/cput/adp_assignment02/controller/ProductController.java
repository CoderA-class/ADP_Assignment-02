package za.ac.cput.adp_assignment02.controller;

/*
ProductController.java
Product Controller class
Author: Isaac Tinotenda Ziyengwa (231269307)
Date: 10 August 2026
*/


import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.service.ProductService;


import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000"})
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @GetMapping("/read/{id}")
    public Product read(@PathVariable String id) {
        return productService.read(id);
    }

    @PutMapping("/update")
    public Product update(@RequestBody Product product) {
        return productService.update(product);
    }

    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable String id) {
        return productService.delete(id);
    }

    @GetMapping("/all")
    public List<Product> getAll() {
        return productService.getAll();
    }
}
