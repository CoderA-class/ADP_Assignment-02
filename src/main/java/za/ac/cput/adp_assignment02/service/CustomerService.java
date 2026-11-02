package za.ac.cput.adp_assignment02.service;

/*
CustomerService.java
Customer Service
Implementation Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
Updated: auto-generates the customer ID, only a name is required, search by name.
*/


import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.time.LocalDate;
import java.util.List;

@Service
public class CustomerService implements ICustomerService {
    private final CustomerRepository repository;
    private final IdGeneratorService idGenerator;

    public CustomerService(CustomerRepository repository, IdGeneratorService idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    @Override
    public Customer create(Customer customer) {
        validate(customer);
        if (Helper.isBlank(customer.getCustID())) {
            customer.setCustID(idGenerator.nextId(IdGeneratorService.CUSTOMER, repository::existsById));
        } else if (repository.existsById(customer.getCustID())) {
            throw new IllegalStateException("A customer with ID " + customer.getCustID() + " already exists.");
        }
        if (customer.getCreatedDate() == null) {
            customer.setCreatedDate(LocalDate.now());
        }
        normalise(customer);
        return repository.save(customer);
    }

    @Override
    public Customer read(String custID) {
        return repository.findById(custID).orElse(null);
    }

    @Override
    public Customer update(Customer customer) {
        if (Helper.isBlank(customer.getCustID())) {
            throw new IllegalArgumentException("Customer ID is required to update a customer.");
        }
        Customer existing = repository.findById(customer.getCustID())
                .orElseThrow(() -> new java.util.NoSuchElementException("Customer " + customer.getCustID() + " was not found."));
        validate(customer);
        if (customer.getCreatedDate() == null) {
            customer.setCreatedDate(existing.getCreatedDate());
        }
        if (customer.getLastPurchaseDate() == null) {
            customer.setLastPurchaseDate(existing.getLastPurchaseDate());
        }
        normalise(customer);
        return repository.save(customer);
    }

    /* Called by the Point of Sale after a successful sale */
    public void recordPurchase(String custID) {
        repository.findById(custID).ifPresent(c -> {
            c.setLastPurchaseDate(LocalDate.now());
            repository.save(c);
        });
    }

    @Override
    public boolean delete(String custID) {
        if (repository.existsById(custID)) {
            repository.deleteById(custID);
            return true;
        }
        return false;
    }

    @Override
    public List<Customer> getAll() {
        return repository.findAll();
    }

    public List<Customer> searchByName(String name) {
        return Helper.isBlank(name) ? getAll() : repository.searchByName(name.trim());
    }

    private void validate(Customer customer) {
        if (customer == null || Helper.isBlank(customer.getFirstName())) {
            throw new IllegalArgumentException("Customer name is required.");
        }
    }

    private void normalise(Customer c) {
        c.setFirstName(Helper.clean(c.getFirstName()));
        c.setLastName(Helper.clean(c.getLastName()));
        c.setEmail(Helper.clean(c.getEmail()));
        c.setPhoneNumber(Helper.clean(c.getPhoneNumber()));
        c.setAddress(Helper.clean(c.getAddress()));
        c.setCompany(Helper.clean(c.getCompany()));
    }
}
