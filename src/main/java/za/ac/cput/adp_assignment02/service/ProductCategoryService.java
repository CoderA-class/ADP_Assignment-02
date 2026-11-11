package za.ac.cput.adp_assignment02.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.ProductCategory;
import za.ac.cput.adp_assignment02.repository.ProductCategoryRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/*
 * ProductCategoryService.java
 * Admins add, rename and remove stock categories. Categories that products already use
 * (from before this feature existed) are picked up automatically.
 */
@Service
public class ProductCategoryService {

    private static final Pattern HEX = Pattern.compile("^#[0-9a-fA-F]{6}$");
    private static final Pattern ICON = Pattern.compile("^[a-zA-Z]{1,30}$");

    private final ProductCategoryRepository repository;
    private final ProductRepository productRepository;

    public ProductCategoryService(ProductCategoryRepository repository, ProductRepository productRepository) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<ProductCategory> getAll() {
        // make sure every category that a product uses is listed
        for (Product p : productRepository.findAll()) {
            String name = clean(p.getCategory());
            if (name != null && repository.findByNameIgnoreCase(name).isEmpty()) {
                repository.save(new ProductCategory(name, null, null));
            }
        }
        return repository.findAllByOrderByNameAsc();
    }

    public ProductCategory create(String name, String icon, String color) {
        String clean = requireName(name);
        if (repository.findByNameIgnoreCase(clean).isPresent()) {
            throw new IllegalStateException("There is already a category called " + clean + ".");
        }
        return repository.save(new ProductCategory(clean, cleanIcon(icon), cleanColor(color)));
    }

    /* Renaming a category also moves its products to the new name */
    @Transactional
    public ProductCategory update(Long id, String name, String icon, String color) {
        ProductCategory category = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Category was not found."));
        String clean = requireName(name);
        repository.findByNameIgnoreCase(clean).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new IllegalStateException("There is already a category called " + clean + ".");
            }
        });
        String oldName = category.getName();
        if (!oldName.equals(clean)) {
            for (Product p : productRepository.findByCategoryIgnoreCase(oldName)) {
                p.setCategory(clean);
                productRepository.save(p);
            }
        }
        category.setName(clean);
        category.setIcon(cleanIcon(icon));
        category.setColor(cleanColor(color));
        return repository.save(category);
    }

    public boolean delete(Long id) {
        ProductCategory category = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Category was not found."));
        int inUse = productRepository.findByCategoryIgnoreCase(category.getName()).size();
        if (inUse > 0) {
            throw new IllegalStateException(inUse + " product" + (inUse == 1 ? " still uses " : "s still use ") + category.getName()
                    + ". Move " + (inUse == 1 ? "it" : "them") + " to another category first.");
        }
        repository.delete(category);
        return true;
    }

    private static String clean(String name) {
        if (name == null) return null;
        String trimmed = name.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String requireName(String name) {
        String clean = clean(name);
        if (clean == null) {
            throw new IllegalArgumentException("Please enter a category name.");
        }
        if (clean.length() > 40) {
            throw new IllegalArgumentException("Keep the category name under 40 characters.");
        }
        return clean;
    }

    private static String cleanIcon(String icon) {
        return icon != null && ICON.matcher(icon).matches() ? icon : null;
    }

    private static String cleanColor(String color) {
        return color != null && HEX.matcher(color).matches() ? color.toLowerCase() : null;
    }
}
