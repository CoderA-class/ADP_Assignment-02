package za.ac.cput.adp_assignment02.controller;

/*
CustomerController.java
Customer Controller class
Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.service.CustomerService;
import za.ac.cput.adp_assignment02.service.OrderService;


import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final OrderService orderService;

    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    /* The customer ID is generated automatically - leave custID empty */
    @PostMapping("/create")
    public Customer create(@RequestBody Customer customer) {
        return customerService.create(customer);
    }

    @GetMapping("/read/{custID}")
    public ResponseEntity<Customer> read(@PathVariable String custID) {
        Customer customer = customerService.read(custID);
        return customer == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(customer);
    }

    @PutMapping("/update")
    public Customer update(@RequestBody Customer customer) {
        return customerService.update(customer);
    }

    @DeleteMapping("/delete/{custID}")
    public boolean delete(@PathVariable String custID) {
        return customerService.delete(custID);
    }

    @GetMapping("/all")
    public List<Customer> getAll() {
        return customerService.getAll();
    }

    /* e.g. /customers/search?name=john */
    @GetMapping("/search")
    public List<Customer> search(@RequestParam(defaultValue = "") String name) {
        return customerService.searchByName(name);
    }

    @GetMapping("/{custID}/orders")
    public List<Order> orders(@PathVariable String custID) {
        return orderService.getForCustomer(custID);
    }
}
