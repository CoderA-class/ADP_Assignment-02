package za.ac.cput.adp_assignment02.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/*
 * HealthController.java
 * Lets the frontend check that the backend is running (GET /health).
 * The React app polls this so it doesn't matter which one you start first.
 */
@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "time", LocalDateTime.now().toString());
    }
}
