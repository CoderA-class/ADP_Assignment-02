package za.ac.cput.adp_assignment02.service;

/*
OrderItemService.java
OrderItem Service
Implementation Author: Adriaan van der Westhuizen (240436415)
Date: 10 August 2026
*/


import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;


import java.util.List;

@Service
public class OrderItemService implements IOrderItemService {
    private final OrderItemRepository repository;

    public OrderItemService(OrderItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        return repository.save(orderItem);
    }

    @Override
    public OrderItem read(String orderItemId) {
        return repository.findById(orderItemId).orElse(null);
    }

    @Override
    public OrderItem update(OrderItem orderItem) {
        return repository.save(orderItem);
    }

    @Override
    public boolean delete(String orderItemId) {
        if (repository.existsById(orderItemId)) {
            repository.deleteById(orderItemId);
            return true;
        }
        return false;
    }

    @Override
    public List<OrderItem> getAll() {
        return repository.findAll();
    }
}