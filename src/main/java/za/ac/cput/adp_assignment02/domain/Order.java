package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
* Order.java
* Order model class
* Author: Rocco Given Visagie (220343527)
* Date: 23 March 2026
*/

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "orderId")
    private String orderId;
    @Column(nullable = false)
    private String customerId;
    @Column(nullable = false)
    private LocalDate orderDate;
    @Column(nullable = false)
    private double totalAmount;
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>(); /* * Default constructor required by JPA. */

    protected Order() {
    }

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.orderDate = builder.orderDate;
        this.totalAmount = builder.totalAmount;
        this.orderItems = builder.orderItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
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
        private LocalDate orderDate;
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

        public Builder setOrderDate(LocalDate orderDate) {
            this.orderDate = orderDate;
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
