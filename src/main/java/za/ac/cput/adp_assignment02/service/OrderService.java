package za.ac.cput.adp_assignment02.service;

/*
OrderService.java
Order Service Implementation
Author: Rocco Given Visagie (220343527)
Date: 10 August 2026
*/


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.OrderRepository;


import java.util.List;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository repository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository repository, CustomerRepository customerRepository) {
        this.repository = repository;
        this.customerRepository = customerRepository;
    }

    @Override
    public Order create(Order order) {
        validateCustomer(order);
        return repository.save(order);
    }

    @Override
    public Order read(String orderId) {
        return repository.findById(orderId).orElse(null);
    }

    @Override
    public Order update(Order order) {
        validateCustomer(order);
        return repository.save(order);
    }

    @Override
    public boolean delete(String orderId) {
        if (repository.existsById(orderId)) {
            repository.deleteById(orderId);
            return true;
        }
        return false;
    }

    @Override
    public List<Order> getAll() {
        return repository.findAll();
    }

    private void validateCustomer(Order order) {
        String customerId = order.getCustomerId();
        if (customerId == null || customerId.isBlank() || !customerRepository.existsById(customerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer does not exist: " + customerId);
        }
    }
}