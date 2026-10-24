package za.ac.cput.adp_assignment02.service;

/*
CustomerService.java
Customer Service
Implementation Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/


import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.util.IdGenerator;


import java.util.List;

@Service
public class CustomerService implements ICustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Customer create(Customer customer) {
        if (customer.getCustID() == null || customer.getCustID().isBlank()) {
            customer.setCustID(IdGenerator.generate("CUST"));
        }
        return repository.save(customer);
    }

    @Override
    public Customer read(String custID) {
        return repository.findById(custID).orElse(null);
    }

    @Override
    public Customer update(Customer customer) {
        return repository.save(customer);
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
}