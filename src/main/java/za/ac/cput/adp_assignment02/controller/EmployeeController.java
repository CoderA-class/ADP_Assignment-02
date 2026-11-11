package za.ac.cput.adp_assignment02.controller;

/*
EmployeeController.java
Employee Controller class
Author: Robyn Dominique Stevens (222201789)
Date: 10 August 2026
*/


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.NotificationService;
import za.ac.cput.adp_assignment02.service.EmployeeService;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")   // method security (Blackboard notes 9.7.6): only admins manage staff
public class EmployeeController {
    private final EmployeeService employeeService;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;

    public EmployeeController(EmployeeService employeeService, CurrentUser currentUser, NotificationService notificationService) {
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    /* Ban / unban: a banned employee can't sign in and is signed out immediately */
    @PutMapping("/{empID}/ban")
    public Employee ban(@PathVariable String empID) {
        return employeeService.ban(empID, currentUser.require().getEmpID());
    }

    @PutMapping("/{empID}/unban")
    public Employee unban(@PathVariable String empID) {
        return employeeService.unban(empID);
    }

    /* Returns a one-time password for the admin to pass on; the employee must change it when they sign in */
    @PostMapping("/{empID}/reset-password")
    public Map<String, String> resetPassword(@PathVariable String empID) {
        String temporary = employeeService.resetPassword(empID);
        notificationService.markHandled(Notification.Type.PASSWORD_RESET, empID);
        return Map.of("temporaryPassword", temporary);
    }

    /* The employee ID is generated automatically - leave empID empty */
    @PostMapping("/create")
    public Employee create(@RequestBody Employee employee) {
        return employeeService.create(employee);
    }

    @GetMapping("/read/{empID}")
    public ResponseEntity<Employee> read(@PathVariable String empID) {
        Employee employee = employeeService.read(empID);
        return employee == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(employee);
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

    @GetMapping("/search")
    public List<Employee> search(@RequestParam(defaultValue = "") String name) {
        return employeeService.searchByName(name);
    }
}
