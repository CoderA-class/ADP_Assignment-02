package za.ac.cput.adp_assignment02.controller;

/*
EmployeeController.java
Employee Controller class
Author: Robyn Dominique Stevens (222201789)
Date: 10 August 2026
*/


import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.service.EmployeeService;


import java.util.List;

@RestController
@RequestMapping("/employees")
@CrossOrigin(origins = "http://localhost:5173")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/create")
    public Employee create(@RequestBody Employee employee) {
        return employeeService.create(employee);
    }

    @GetMapping("/read/{empID}")
    public Employee read(@PathVariable String empID) {
        return employeeService.read(empID);
    }

    @PutMapping("/update")
    public Employee update(@RequestBody Employee employee) {
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
}