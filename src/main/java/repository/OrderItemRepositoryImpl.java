package repository;

/*
        OrderItemRepositoryImpl.java
        Author: Adriaan van der Westhuizen (240436415)
        Date: 26 March 2026
*/

import domain.OrderItem;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OrderItemRepositoryImpl implements OrderItemRepository {

    private static OrderItemRepositoryImpl repository = null;
    private final Map<String, OrderItem> orderItemTable;

    private OrderItemRepositoryImpl() {
        orderItemTable = new HashMap<>();
    }

    public static OrderItemRepositoryImpl getRepository() {
        if (repository == null)
            repository = new OrderItemRepositoryImpl();
        return repository;
    }

    @Override
    public OrderItem create(OrderItem orderItem) {
        orderItemTable.put(orderItem.getOrderItemId(), orderItem);
        return orderItem;
    }


    @Override
    public OrderItem read(String id) {
        return orderItemTable.get(id);
    }

    @Override
    public OrderItem update(OrderItem orderItem) {
        if (orderItemTable.containsKey(orderItem.getOrderItemId())) {
            orderItemTable.put(orderItem.getOrderItemId(), orderItem);
            return orderItem;
        }
        return null;
    }

    @Override
    public boolean delete(String id) {
        OrderItem removed = orderItemTable.remove(id);
        return removed != null;
    }

    @Override
    public Set<OrderItem> getAll() {
        return Set.of();
    }
}