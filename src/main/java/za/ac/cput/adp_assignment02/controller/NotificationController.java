package za.ac.cput.adp_assignment02.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.NotificationService;

import java.util.List;
import java.util.Map;

/*
 * NotificationController.java
 * GET /notifications returns the logged-in user's notifications (worked out from their login,
 * so nobody can read someone else's by changing the URL).
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService, CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    private Employee.AccessLevel myLevel() {
        return currentUser.isAdmin() ? Employee.AccessLevel.ADMIN : Employee.AccessLevel.EMPLOYEE;
    }

    @GetMapping
    public List<Notification> list() {
        return notificationService.getFor(currentUser.require().getEmpID(), myLevel());
    }

    @PutMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }

    @PutMapping("/read-all")
    public Map<String, Integer> markAllRead() {
        return Map.of("updated", notificationService.markAllRead(currentUser.require().getEmpID(), myLevel()));
    }

    /* Run the low-stock / inactive-customer checks right now */
    @PostMapping("/run-checks")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> runChecks() {
        notificationService.runAutomaticChecks();
        return Map.of("message", "Checks complete.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public boolean delete(@PathVariable Long id) {
        return notificationService.delete(id);
    }
}
