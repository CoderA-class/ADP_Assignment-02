package za.ac.cput.adp_assignment02.service;

/*
SupplierService.java
Supplier Service Implementation
Author: Luke John Zyster (220489114)
Date: 10 August 2026
Updated: auto-generates the supplier ID, only the name is required, search by name.
*/

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.repository.SupplierRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SupplierService implements ISupplierService {
    private final SupplierRepository repository;
    private final ProductRepository productRepository;
    private final IdGeneratorService idGenerator;

    public SupplierService(SupplierRepository repository, ProductRepository productRepository, IdGeneratorService idGenerator) {
        this.repository = repository;
        this.productRepository = productRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Supplier create(Supplier supplier) {
        validate(supplier);
        if (Helper.isBlank(supplier.getSupplierId())) {
            supplier.setSupplierId(idGenerator.nextId(IdGeneratorService.SUPPLIER, repository::existsById));
        } else if (repository.existsById(supplier.getSupplierId())) {
            throw new IllegalStateException("A supplier with ID " + supplier.getSupplierId() + " already exists.");
        }
        normalise(supplier);
        return repository.save(supplier);
    }

    @Override
    public Supplier read(String supplierId) {
        return repository.findById(supplierId).orElse(null);
    }

    @Override
    public Supplier update(Supplier supplier) {
        if (Helper.isBlank(supplier.getSupplierId()) || !repository.existsById(supplier.getSupplierId())) {
            throw new NoSuchElementException("Supplier was not found.");
        }
        validate(supplier);
        normalise(supplier);
        return repository.save(supplier);
    }

    /* Deleting a supplier keeps its products but removes the link to the supplier */
    @Override
    @Transactional
    public boolean delete(String supplierId) {
        if (repository.existsById(supplierId)) {
            for (Product product : productRepository.findBySupplierId(supplierId)) {
                product.setSupplierId(null);
                productRepository.save(product);
            }
            repository.deleteById(supplierId);
            return true;
        }
        return false;
    }

    @Override
    public List<Supplier> getAll() {
        return repository.findAll();
    }

    public List<Supplier> searchByName(String name) {
        return Helper.isBlank(name) ? getAll() : repository.searchByName(name.trim());
    }

    private void validate(Supplier supplier) {
        if (supplier == null || Helper.isBlank(supplier.getName())) {
            throw new IllegalArgumentException("Supplier name is required.");
        }
    }

    private void normalise(Supplier s) {
        s.setName(Helper.clean(s.getName()));
        s.setContactPerson(Helper.clean(s.getContactPerson()));
        s.setEmail(Helper.clean(s.getEmail()));
        s.setPhone(Helper.clean(s.getPhone()));
        s.setAddress(Helper.clean(s.getAddress()));
    }
}
