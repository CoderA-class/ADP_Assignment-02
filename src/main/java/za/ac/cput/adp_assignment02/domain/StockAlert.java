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
 * StockAlert.java
 * An alert is resolved automatically as soon as the product is back above its re-order level.
 * Author: Adriaan van der Westhuizen (240436415)
 * Date: 11 November 2026
 */

@Entity
@Table(name = "stock_alert")
public class StockAlert {

    public enum Status {OPEN, ACKNOWLEDGED, RESOLVED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String productId;
    @Column(nullable = false)
    private String productName;
    /* how many were left when the alert was raised */
    private int quantityAtReport;
    private int reorderLevel;
    @Column(length = 500)
    private String note;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private String raisedById;
    @Column(nullable = false)
    private String raisedByName;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private String handledByName;
    private LocalDateTime handledAt;
    @Column(length = 500)
    private String resolution;

    public StockAlert() {
    }

    public StockAlert(Product product, Employee raisedBy, String note) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.quantityAtReport = product.getQuantity();
        this.reorderLevel = product.getReorderLevel();
        this.note = note;
        this.status = Status.OPEN;
        this.raisedById = raisedBy.getEmpID();
        this.raisedByName = raisedBy.getFullName();
        this.createdAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status != Status.RESOLVED;
    }

    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public int getQuantityAtReport() { return quantityAtReport; }
    public int getReorderLevel() { return reorderLevel; }
    public String getNote() { return note; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRaisedById() { return raisedById; }
    public String getRaisedByName() { return raisedByName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getHandledByName() { return handledByName; }
    public void setHandledByName(String handledByName) { this.handledByName = handledByName; }
    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
