package za.ac.cput.adp_assignment02.controller;

/*
SupplierController.java
Supplier Controller class
Author: Luke John Zyster (220489114)
Date: 10 August 2026
*/


import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.service.SupplierService;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://localhost:3000"})
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping("/create")
    public Supplier create(@RequestBody Supplier supplier) {
        return supplierService.create(supplier);
    }

    @GetMapping("/read/{supplierId}")
    public Supplier read(@PathVariable String supplierId) {
        return supplierService.read(supplierId);
    }

    @PutMapping("/update")
    public Supplier update(@RequestBody Supplier supplier) {
        return supplierService.update(supplier);
    }

    @DeleteMapping("/delete/{supplierId}")
    public boolean delete(@PathVariable String supplierId) {
        return supplierService.delete(supplierId);
    }

    @GetMapping("/all")
    public List<Supplier> getAll() {
        return supplierService.getAll();
    }
}