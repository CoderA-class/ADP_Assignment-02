package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
* Order.java
* Order model class
* Author: Rocco Given Visagie (220343527)
* Date: 23 March 2026
* Updated: auto-generated order ID (ORD-0001), stores who processed the sale,
*          the exact time of the sale and a snapshot of the customer's name.
*/

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "orderId")
    private String orderId;
    @Column(nullable = false)
    private String customerId;
    private String customerName;
    private String employeeId;
    private String employeeName;
    @Column(nullable = false)
    private LocalDate orderDate;
    private LocalDateTime orderTime;
    /* totalAmount = subtotal + VAT. subtotal / vatAmount are empty on orders made before VAT was added */
    @Column(nullable = false)
    private double totalAmount;
    private Double subtotal;
    private Double vatAmount;
    private Double vatRate;
    /* CASH, CARD, EFT or ACCOUNT. Empty on orders made before payments were tracked (treated as paid) */
    @Column(length = 20)
    private String paymentMethod;
    /* how much of the total has been paid so far (account sales start at 0) */
    private Double amountPaid;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>(); /* * Default constructor required by JPA. */

    public Order() {
    }

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.customerName = builder.customerName;
        this.employeeId = builder.employeeId;
        this.employeeName = builder.employeeName;
        this.orderDate = builder.orderDate;
        this.orderTime = builder.orderTime;
        this.totalAmount = builder.totalAmount;
        this.orderItems = builder.orderItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Double getAmountPaid() { return amountPaid == null ? totalAmount : amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

    /* what the customer still owes on this order */
    public double getBalanceDue() {
        return Math.max(0, Math.round((totalAmount - getAmountPaid()) * 100.0) / 100.0);
    }

    /* PAID, PARTIAL or UNPAID */
    public String getPaymentStatus() {
        double due = getBalanceDue();
        if (due <= 0) return "PAID";
        return getAmountPaid() > 0 ? "PARTIAL" : "UNPAID";
    }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getVatAmount() { return vatAmount; }
    public void setVatAmount(Double vatAmount) { this.vatAmount = vatAmount; }
    public Double getVatRate() { return vatRate; }
    public void setVatRate(Double vatRate) { this.vatRate = vatRate; }

    /* Sets subtotal, VAT and the grand total from a subtotal (amounts rounded to cents) */
    public void applyVat(double subtotalAmount, double rate) {
        double sub = Math.round(subtotalAmount * 100.0) / 100.0;
        double vat = Math.round(sub * rate * 100.0) / 100.0;
        this.subtotal = sub;
        this.vatRate = rate;
        this.vatAmount = vat;
        this.totalAmount = Math.round((sub + vat) * 100.0) / 100.0;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public double calculateTotal() {
        double total = 0;
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                total += item.getLineTotal();
            }
        }
        return total;
    }

    public static class Builder {
        private String orderId;
        private String customerId;
        private String customerName;
        private String employeeId;
        private String employeeName;
        private LocalDate orderDate;
        private LocalDateTime orderTime;
        private double totalAmount;
        private List<OrderItem> orderItems = new ArrayList<>();

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setCustomerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
            return this;
        }

        public Builder setOrderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder setOrderTime(LocalDateTime orderTime) {
            this.orderTime = orderTime;
            return this;
        }

        public Builder setTotalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder setOrderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public Builder addOrderItem(OrderItem item) {
            this.orderItems.add(item);
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
