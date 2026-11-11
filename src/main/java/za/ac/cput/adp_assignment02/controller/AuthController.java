package za.ac.cput.adp_assignment02.controller;

import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.dto.ChangePasswordRequest;
import za.ac.cput.adp_assignment02.service.NotificationService;
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

    private final NotificationService notificationService;

    public AuthController(EmployeeService employeeService, CurrentUser currentUser, NotificationService notificationService) {
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.notificationService = notificationService;
    }

    /* "Forgot password?" on the login page. There's no email server, so the request goes to the admins,
       who reset it from the Employees page. The answer is the same whether or not the email exists. */
    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        Employee employee = employeeService.findByEmail(body == null ? null : body.get("email"));
        if (employee != null && employee.getEmpStatus() == Employee.EmpStatus.ACTIVE) {
            notificationService.notifyOnce(Notification.Type.PASSWORD_RESET, "Password reset request",
                    employee.getFullName() + " (" + employee.getEmail() + ") can't sign in and asked for a new password. "
                            + "Open Employees and choose Reset password.",
                    employee.getEmpID(), Notification.AUDIENCE_ADMIN);
        }
        return Map.of("message", "If that email belongs to an active account, an admin has been asked to reset the password. "
                + "They'll give you a temporary password to sign in with.");
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
