package repository;

/*
  OrderRepositoryImpl.java
  OrderRepositoryImpl model class
  Author: Rocco Given Visagie (220343527)
  Date: 23 March 2026
 */

import domain.Order;
import repository.OrderRepository;
import repository.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepositoryImpl implements OrderRepository {

    private static OrderRepositoryImpl repository = null;
    private final Map<String, Order> orderTable;

    private OrderRepositoryImpl() {
        orderTable = new HashMap<>();
    }

    public static OrderRepositoryImpl getRepository() {
        if (repository == null) {
            repository = new OrderRepositoryImpl();
        }
        return repository;
    }

    @Override
    public Order create(Order order) {
        orderTable.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Order read(String orderId) {
        return orderTable.get(orderId);
    }

    @Override
    public Order update(Order order) {
        if (orderTable.containsKey(order.getOrderId())) {
            orderTable.replace(order.getOrderId(), order);
            return order;
        }
        return null;
    }

    @Override
    public boolean delete(String orderId) {
        if (orderTable.containsKey(orderId)) {
            orderTable.remove(orderId);
            return true;
        }
        return false;
    }

    @Override
    public List<Order> getAll() {
        return new ArrayList<>(orderTable.values());
    }
}