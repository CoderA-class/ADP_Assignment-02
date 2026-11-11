package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/*
 * Notification.java
 * An in-app notification (low stock, inactive customer, leave request, clock-in, ...).
 * audience is "ADMIN" (all admins), "ALL" (everyone) or a specific employee ID.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@Entity
@Table(name = "notification")
public class Notification {

    public enum Type {LOW_STOCK, OUT_OF_STOCK, INACTIVE_CUSTOMER, LEAVE_REQUEST, LEAVE_DECISION, CLOCK_IN, CLOCK_OUT, NEW_SALE, STOCK_ALERT, STOCK_ALERT_UPDATE, PASSWORD_RESET, SYSTEM}

    public static final String AUDIENCE_ADMIN = "ADMIN";
    public static final String AUDIENCE_ALL = "ALL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /* stored as plain text (not a MySQL ENUM) so new notification types can be added later */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, columnDefinition = "varchar(40)")
    private Type type;
    @Column(nullable = false)
    private String title;
    @Column(length = 1000)
    private String message;
    /* ID of the related record (product, customer, leave request, ...) */
    private String refId;
    @Column(nullable = false)
    private String audience;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    /* "read" is a reserved word in MySQL, so the column is called "seen" */
    @Column(nullable = false)
    private boolean seen;

    public Notification() {
    }

    public Notification(Type type, String title, String message, String refId, String audience) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.refId = refId;
        this.audience = audience;
        this.createdAt = LocalDateTime.now();
        this.seen = false;
    }

    public Long getId() { return id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }
}
