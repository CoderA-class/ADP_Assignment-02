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
    @Column(nullable = false)
    private double totalAmount;
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
