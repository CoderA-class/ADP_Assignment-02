package za.ac.cput.adp_assignment02.controller;

/*
OrderItemController.java
OrderItem Controller class
Author: Adriaan van der Westhuizen (240436415)
Date: 10 August 2026
*/


import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.service.OrderItemService;


import java.util.List;

@RestController
@RequestMapping("/order-items")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000"})
public class OrderItemController {
    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @PostMapping("/create")
    public OrderItem create(@RequestBody OrderItem orderItem) {
        return orderItemService.create(orderItem);
    }

    @GetMapping("/read/{orderItemId}")
    public OrderItem read(@PathVariable String orderItemId) {
        return orderItemService.read(orderItemId);
    }

    @PutMapping("/update")
    public OrderItem update(@RequestBody OrderItem orderItem) {
        return orderItemService.update(orderItem);
    }

    @DeleteMapping("/delete/{orderItemId}")
    public boolean delete(@PathVariable String orderItemId) {
        return orderItemService.delete(orderItemId);
    }

    @GetMapping("/all")
    public List<OrderItem> getAll() {
        return orderItemService.getAll();
    }
}