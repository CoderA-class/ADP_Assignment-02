package za.ac.cput.adp_assignment02.controller;

import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Order;
import za.ac.cput.adp_assignment02.domain.Payment;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.PaymentService;

import java.util.List;
import java.util.Map;

/*
 * PaymentController.java
 * POST /payments/order/{orderId}   { amount (empty = pay it all off), method: CASH|CARD|EFT, note }
 * GET  /payments                   all payments            GET /payments/customer/{id}   one customer
 * GET  /payments/order/{orderId}   one order               GET /payments/balances        who owes what
 * Admins and employees can both take payments at the counter.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService service;
    private final CurrentUser currentUser;

    public PaymentController(PaymentService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @PostMapping("/order/{orderId}")
    public Order pay(@PathVariable String orderId, @RequestBody Map<String, Object> body) {
        Object raw = body == null ? null : body.get("amount");
        Double amount;
        try {
            amount = raw == null || String.valueOf(raw).isBlank() ? null : Double.valueOf(String.valueOf(raw));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter the amount as a number, e.g. 250.00");
        }
        return service.recordPayment(orderId, amount,
                body == null ? null : (String) body.get("method"),
                body == null ? null : (String) body.get("note"),
                currentUser.require());
    }

    @GetMapping
    public List<Payment> all() {
        return service.all();
    }

    @GetMapping("/order/{orderId}")
    public List<Payment> forOrder(@PathVariable String orderId) {
        return service.forOrder(orderId);
    }

    @GetMapping("/customer/{customerId}")
    public List<Payment> forCustomer(@PathVariable String customerId) {
        return service.forCustomer(customerId);
    }

    @GetMapping("/balances")
    public Map<String, Double> balances() {
        return service.balances();
    }
}
