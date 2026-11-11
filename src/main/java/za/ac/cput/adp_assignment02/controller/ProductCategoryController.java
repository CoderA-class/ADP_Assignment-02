package za.ac.cput.adp_assignment02.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.ProductCategory;
import za.ac.cput.adp_assignment02.service.ProductCategoryService;

import java.util.List;

/*
 * ProductCategoryController.java
 * GET /categories for everyone; adding, renaming and removing categories is for admins.
 */
@RestController
@RequestMapping("/categories")
public class ProductCategoryController {
    private final ProductCategoryService service;

    public ProductCategoryController(ProductCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductCategory> list() {
        return service.getAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductCategory create(@RequestBody ProductCategory category) {
        return service.create(category.getName(), category.getIcon(), category.getColor());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductCategory update(@PathVariable Long id, @RequestBody ProductCategory category) {
        return service.update(id, category.getName(), category.getIcon(), category.getColor());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
