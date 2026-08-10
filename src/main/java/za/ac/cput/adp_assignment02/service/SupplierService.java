package za.ac.cput.adp_assignment02.service;

/*
SupplierService.java
Supplier Service Implementation
Author: Luke John Zyster (220489114)
Date: 10 August 2026
*/


import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.repository.SupplierRepository;


import java.util.List;

@Service
public class SupplierService implements ISupplierService {
    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    @Override
    public Supplier create(Supplier supplier) {
        return repository.save(supplier);
    }

    @Override
    public Supplier read(String supplierId) {
        return repository.findById(supplierId).orElse(null);
    }

    @Override
    public Supplier update(Supplier supplier) {
        return repository.save(supplier);
    }

    @Override
    public boolean delete(String supplierId) {
        if (repository.existsById(supplierId)) {
            repository.deleteById(supplierId);
            return true;
        }
        return false;
    }

    @Override
    public List<Supplier> getAll() {
        return repository.findAll();
    }
}
