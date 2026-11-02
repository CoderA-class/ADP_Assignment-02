package za.ac.cput.adp_assignment02.controller;

/*
ProductController.java
Product Controller class
Author: Isaac Tinotenda Ziyengwa (231269307)
Date: 10 August 2026
*/


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.service.ProductService;


import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /* The product ID is generated automatically - leave id empty */
    @PostMapping("/create")
    public Product create(@RequestBody Product product) {
        return productService.create(product);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Product> read(@PathVariable String id) {
        Product product = productService.read(id);
        return product == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(product);
    }

    @PutMapping("/update")
    public Product update(@RequestBody Product product) {
        return productService.update(product);
    }

    /* e.g. PUT /products/PRD-0001/restock?amount=50 */
    @PutMapping("/{id}/restock")
    public Product restock(@PathVariable String id, @RequestParam int amount) {
        return productService.restock(id, amount);
    }

    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable String id) {
        return productService.delete(id);
    }

    @GetMapping("/all")
    public List<Product> getAll() {
        return productService.getAll();
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam(defaultValue = "") String name) {
        return productService.searchByName(name);
    }

    @GetMapping("/low-stock")
    public List<Product> lowStock() {
        return productService.getLowStock();
    }
}
