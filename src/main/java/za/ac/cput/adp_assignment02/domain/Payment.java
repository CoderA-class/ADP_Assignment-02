package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/*
 * Payment.java
 * Money received against an account (pay-later) order: the full balance or part of it.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String orderId;
    @Column(nullable = false)
    private String customerId;
    private String customerName;
    @Column(nullable = false)
    private double amount;
    /* how it was paid: CASH, CARD or EFT */
    @Column(length = 20)
    private String method;
    private String receivedBy;
    @Column(nullable = false)
    private LocalDateTime paidAt;
    @Column(length = 200)
    private String note;

    public Payment() {
    }

    public Payment(Order order, double amount, String method, String receivedBy, String note) {
        this.orderId = order.getOrderId();
        this.customerId = order.getCustomerId();
        this.customerName = order.getCustomerName();
        this.amount = amount;
        this.method = method;
        this.receivedBy = receivedBy;
        this.note = note;
        this.paidAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public double getAmount() { return amount; }
    public String getMethod() { return method; }
    public String getReceivedBy() { return receivedBy; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public String getNote() { return note; }
}
