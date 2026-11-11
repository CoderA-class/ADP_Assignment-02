package za.ac.cput.adp_assignment02.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.StockAlert;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.StockAlertService;

import java.util.List;
import java.util.Map;

/*
 * StockAlertController.java
 * POST /stock-alerts                 any logged-in user alerts the admins about low stock
 * GET  /stock-alerts                 everyone can see alerts (so nobody reports the same thing twice)
 * PUT  /stock-alerts/{id}/acknowledge   admin: "I'm on it"
 * PUT  /stock-alerts/{id}/resolve       admin: close the alert
 */
@RestController
@RequestMapping("/stock-alerts")
public class StockAlertController {
    private final StockAlertService service;
    private final CurrentUser currentUser;

    public StockAlertController(StockAlertService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping
    public StockAlert raise(@RequestBody Map<String, String> body) {
        String productId = body.get("productId");
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Choose the product that is running low.");
        }
        return service.raise(productId, body.get("note"), currentUser.require());
    }

    @GetMapping
    public List<StockAlert> list() {
        return service.getAll();
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('ADMIN')")
    public StockAlert acknowledge(@PathVariable Long id) {
        return service.acknowledge(id, currentUser.require());
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public StockAlert resolve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return service.resolve(id, body == null ? null : body.get("resolution"), currentUser.require());
    }
}
