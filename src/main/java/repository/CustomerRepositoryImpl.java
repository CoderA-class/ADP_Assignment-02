package repository;

import domain.Customer;
import repository.CustomerRepository;

import java.util.HashSet;
import java.util.Set;

public class CustomerRepositoryImpl implements CustomerRepository {

    private static CustomerRepository repository = null;
    private Set<Customer> customerDB = null;

    private CustomerRepositoryImpl() {
        customerDB = new HashSet<>();
    }

    public static CustomerRepository getRepository() {
        if (repository == null) {
            repository = new CustomerRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Customer create(Customer customer) {
        customerDB.add(customer);
        return customer;
    }

    @Override
    public Customer read(String custID) {
        return customerDB.stream()
                .filter(c -> c.getCustID().equals(custID))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Customer update(Customer customer) {
        Customer oldCustomer = read(customer.getCustID());
        if (oldCustomer != null) {
            customerDB.remove(oldCustomer);
            customerDB.add(customer);
            return customer;
        }
        return null;
    }

    @Override
    public boolean delete(String custID) {
        Customer customer = read(custID);
        if (customer != null) {
            customerDB.remove(customer);
            return true;
        }
        return false;
    }

    @Override
    public Set<Customer> getAll() {
        return customerDB;
    }
}