package za.ac.cput.adp_assignment02.service;

/*
ProductService.java
Product Service Implementation
Author: Isaac Tinotenda Ziyengwa (231269307)
Date: 10 August 2026
Updated: auto-generates the product ID, validates input, restocking,
         and raises low-stock notifications automatically.
*/

import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService implements IProductService {
    private final ProductRepository repository;
    private final IdGeneratorService idGenerator;
    private final NotificationService notificationService;

    public ProductService(ProductRepository repository, IdGeneratorService idGenerator, NotificationService notificationService) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.notificationService = notificationService;
    }

    @Override
    public Product create(Product product) {
        validate(product);
        if (Helper.isBlank(product.getId())) {
            product.setId(idGenerator.nextId(IdGeneratorService.PRODUCT, repository::existsById));
        } else if (repository.existsById(product.getId())) {
            throw new IllegalStateException("A product with ID " + product.getId() + " already exists.");
        }
        normalise(product);
        Product saved = repository.save(product);
        notificationService.checkStock(saved);
        return saved;
    }

    @Override
    public Product read(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Product update(Product product) {
        if (Helper.isBlank(product.getId()) || !repository.existsById(product.getId())) {
            throw new NoSuchElementException("Product was not found.");
        }
        validate(product);
        normalise(product);
        Product saved = repository.save(product);
        notificationService.checkStock(saved);
        return saved;
    }

    /* Adds stock (e.g. when a delivery from the supplier arrives) */
    public Product restock(String id, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restock amount must be more than 0.");
        }
        Product product = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Product was not found."));
        product.setQuantity(product.getQuantity() + amount);
        return repository.save(product);
    }

    @Override
    public boolean delete(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Product> getAll() {
        return repository.findAll();
    }

    public List<Product> searchByName(String name) {
        return Helper.isBlank(name) ? getAll() : repository.searchByName(name.trim());
    }

    public List<Product> getLowStock() {
        return repository.findAll().stream().filter(Product::isLowStock).toList();
    }

    private void validate(Product product) {
        if (product == null || Helper.isBlank(product.getName())) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
    }

    private void normalise(Product p) {
        p.setName(p.getName().trim());
        p.setCategory(Helper.isBlank(p.getCategory()) ? "General" : p.getCategory().trim());
        if (Helper.isBlank(p.getSupplierId())) {
            p.setSupplierId(null);
        }
    }
}
