package repository;

import domain.Order;

import java.util.List;

public interface OrderRepository {

    Order create(Order order);

    Order read(String orderId);

    Order update(Order order);

    boolean delete(String orderId);

    List<Order> getAll();
}