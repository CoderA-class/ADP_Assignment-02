package repository;

/*
  OrderRepository.java
  OrderRepository model class
  Author: Rocco Given Visagie (220343527)
  Date: 23 March 2026
 */

import domain.Order;

import java.util.List;

public interface OrderRepository {

    Order create(Order order);

    Order read(String orderId);

    Order update(Order order);

    boolean delete(String orderId);

    List<Order> getAll();
}