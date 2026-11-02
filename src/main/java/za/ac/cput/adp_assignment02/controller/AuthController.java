package za.ac.cput.adp_assignment02.controller;

import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.dto.ChangePasswordRequest;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.EmployeeService;

import java.util.Map;

/*
 * AuthController.java
 * Account endpoints for the logged-in user.
 *
 *   POST /auth/login   (email, password, remember-me)  -> handled by Spring Security (see SecurityConfig)
 *   POST /auth/logout                                   -> handled by Spring Security
 *   GET  /auth/me                                       -> who am I?
 *   PUT  /auth/me                                       -> update my phone / photo
 *   POST /auth/change-password                          -> change my password
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final EmployeeService employeeService;
    private final CurrentUser currentUser;

    public AuthController(EmployeeService employeeService, CurrentUser currentUser) {
        this.employeeService = employeeService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    public Employee me() {
        return currentUser.require();
    }

    @PutMapping("/me")
    public Employee updateMe(@RequestBody Map<String, String> changes) {
        Employee me = currentUser.require();
        return employeeService.updateOwnProfile(me.getEmpID(), changes.get("phone"), changes.get("imageUrl"));
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@RequestBody ChangePasswordRequest request) {
        Employee me = currentUser.require();   // you can only change your OWN password
        employeeService.changePassword(me.getEmpID(), request.currentPassword(), request.newPassword());
        return Map.of("message", "Password updated.");
    }
}
