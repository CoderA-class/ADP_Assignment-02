package domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private String orderId;
    private String customerId;
    private LocalDate orderDate;
    private double totalAmount;
    private List<OrderItem> orderItems;

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