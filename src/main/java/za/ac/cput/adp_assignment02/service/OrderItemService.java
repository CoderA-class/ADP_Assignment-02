package za.ac.cput.adp_assignment02.service;

/*
OrderItemService.java
OrderItem Service
Implementation Author: Adriaan van der Westhuizen (240436415)
Date: 10 August 2026
*/


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;


import java.util.List;

@Service
public class OrderItemService implements IOrderItemService {
    private final OrderItemRepository repository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemService(OrderItemRepository repository, OrderRepository orderRepository,
                            ProductRepository productRepository) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        validateReferences(orderItem);
        return repository.save(orderItem);
    }

    @Override
    public OrderItem read(String orderItemId) {
        return repository.findById(orderItemId).orElse(null);
    }

    @Override
    public OrderItem update(OrderItem orderItem) {
        validateReferences(orderItem);
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

    private void validateReferences(OrderItem orderItem) {
        String orderId = orderItem.getOrderId();
        if (orderId == null || orderId.isBlank() || !orderRepository.existsById(orderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order does not exist: " + orderId);
        }

        String productId = orderItem.getProductId();
        if (productId == null || productId.isBlank() || !productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product does not exist: " + productId);
        }
    }
}