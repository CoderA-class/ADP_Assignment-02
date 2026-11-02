package za.ac.cput.adp_assignment02.controller;

/*
EmployeeController.java
Employee Controller class
Author: Robyn Dominique Stevens (222201789)
Date: 10 August 2026
*/


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.service.EmployeeService;


import java.util.List;

@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")   // method security (Blackboard notes 9.7.6): only admins manage staff
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /* The employee ID is generated automatically - leave empID empty */
    @PostMapping("/create")
    public Employee create(@Valid @RequestBody Employee employee) {
        return employeeService.create(employee);
    }

    @GetMapping("/read/{empID}")
    public ResponseEntity<Employee> read(@PathVariable String empID) {
        Employee employee = employeeService.read(empID);
        return employee == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(employee);
    }

    @PutMapping("/update")
    public Employee update(@Valid @RequestBody Employee employee) {
        return employeeService.update(employee);
    }

    @DeleteMapping("/delete/{empID}")
    public boolean delete(@PathVariable String empID) {
        return employeeService.delete(empID);
    }

    @GetMapping("/all")
    public List<Employee> getAll() {
        return employeeService.getAll();
    }

    @GetMapping("/search")
    public List<Employee> search(@RequestParam(defaultValue = "") String name) {
        return employeeService.searchByName(name);
    }
}
