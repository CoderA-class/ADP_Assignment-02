package za.ac.cput.adp_assignment02.controller;

/*
SupplierController.java
Supplier Controller class
Author: Luke John Zyster (220489114)
Date: 10 August 2026
*/


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.service.SupplierService;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /* The supplier ID is generated automatically - leave supplierId empty */
    @PostMapping("/create")
    public Supplier create(@RequestBody Supplier supplier) {
        return supplierService.create(supplier);
    }

    @GetMapping("/read/{supplierId}")
    public ResponseEntity<Supplier> read(@PathVariable String supplierId) {
        Supplier supplier = supplierService.read(supplierId);
        return supplier == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(supplier);
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

    @GetMapping("/search")
    public List<Supplier> search(@RequestParam(defaultValue = "") String name) {
        return supplierService.searchByName(name);
    }
}
