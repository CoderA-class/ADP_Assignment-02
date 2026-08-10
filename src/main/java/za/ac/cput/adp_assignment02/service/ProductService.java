package za.ac.cput.adp_assignment02.service;

/*
ProductService.java
Product Service Implementation
Author: Isaac Tinotenda Ziyengwa (231269307)
Date: 10 August 2026
*/


import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.ProductRepository;


import java.util.List;

@Service
public class ProductService implements IProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Product create(Product product) {
        return repository.save(product);
    }

    @Override
    public Product read(String id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Product update(Product product) {
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
}
