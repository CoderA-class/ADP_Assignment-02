package za.ac.cput.adp_assignment02.controller;

/*
CustomerController.java
Customer Controller class
Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/


import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.service.CustomerService;


import java.util.List;

@RestController
@RequestMapping("/customers")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000"})
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/create")
    public Customer create(@RequestBody Customer customer) {
        return customerService.create(customer);
    }

    @GetMapping("/read/{custID}")
    public Customer read(@PathVariable String custID) {
        return customerService.read(custID);
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
}
