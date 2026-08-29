package za.ac.cput.adp_assignment02.controller;

/*
OrderController.java
Order Controller class
Author: Rocco Given Visagie (220343527)
Date: 10 August 2026
*/


import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.service.OrderService;


import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000"})
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Order create(@RequestBody Order order) {
        return orderService.create(order);
    }

    @GetMapping("/read/{orderId}")
    public Order read(@PathVariable String orderId) {
        return orderService.read(orderId);
    }

    @PutMapping("/update")
    public Order update(@RequestBody Order order) {
        return orderService.update(order);
    }

    @DeleteMapping("/delete/{orderId}")
    public boolean delete(@PathVariable String orderId) {
        return orderService.delete(orderId);
    }

    @GetMapping("/all")
    public List<Order> getAll() {
        return orderService.getAll();
    }
}
