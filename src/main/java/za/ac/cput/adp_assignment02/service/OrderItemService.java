package za.ac.cput.adp_assignment02.service;

/*
OrderItemService.java
OrderItem Service Implementation
Author: Adriaan van der Westhuizen (240436415)
Date: 10 August 2026
Updated: auto-generates the item ID and fills in the product name, unit price and line total.
*/

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.repository.OrderItemRepository;
import za.ac.cput.adp_assignment02.repository.OrderRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.util.List;

@Service
public class OrderItemService implements IOrderItemService {
    private final OrderItemRepository repository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final IdGeneratorService idGenerator;

    public OrderItemService(OrderItemRepository repository, OrderRepository orderRepository,
                            ProductRepository productRepository, IdGeneratorService idGenerator) {
        this.repository = repository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("An order and a product are required.");
        }
        validateReferences(orderItem);
        if (orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        }
        if (Helper.isBlank(orderItem.getOrderItemId())) {
            orderItem.setOrderItemId(idGenerator.nextId(IdGeneratorService.ORDER_ITEM, repository::existsById));
        }
        Product product = productRepository.findById(orderItem.getProductId()).orElse(null);
        if (product != null) {
            if (Helper.isBlank(orderItem.getProductName())) {
                orderItem.setProductName(product.getName());
            }
            if (orderItem.getUnitPrice() == null) {
                orderItem.setUnitPrice(product.getPrice());
            }
        }
        if (orderItem.getLineTotal() <= 0 && orderItem.getUnitPrice() != null) {
            orderItem.setLineTotal(Math.round(orderItem.getUnitPrice() * orderItem.getQuantity() * 100.0) / 100.0);
        }
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

    /* Rocco Given Visagie: an order item must point at a real order and a real product */
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

    public List<OrderItem> getForOrder(String orderId) {
        return repository.findByOrderId(orderId);
    }
}
