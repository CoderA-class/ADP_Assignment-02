package za.ac.cput.adp_assignment02.controller;

/*
OrderController.java
Order Controller class
Author: Rocco Given Visagie (220343527)
Date: 10 August 2026
*/


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.dto.CheckoutRequest;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.OrderService;


import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final CurrentUser currentUser;

    public OrderController(OrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    /* Point of Sale: { customerId, items: [ { productId, quantity } ] }
       The sale is always recorded against the logged-in employee. */
    @PostMapping("/checkout")
    public Order checkout(@RequestBody CheckoutRequest request) {
        String cashier = currentUser.employee().map(e -> e.getEmpID()).orElse(null);
        return orderService.checkout(new CheckoutRequest(request.customerId(), cashier, request.items(), request.paymentMethod()));
    }

    @PostMapping("/create")
    public Order create(@RequestBody Order order) {
        return orderService.create(order);
    }

    @GetMapping("/read/{orderId}")
    public ResponseEntity<Order> read(@PathVariable String orderId) {
        Order order = orderService.read(orderId);
        return order == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(order);
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

    @GetMapping("/search")
    public List<Order> search(@RequestParam(defaultValue = "") String customerName) {
        return orderService.searchByCustomerName(customerName);
    }
}
