package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/*
* OrderItemService.java
* OrderItemService model class
* Author: Adriaan van der Westhuizen (240436415)
* Date: 26 March 2026
*/

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @Column(name = "orderItemId")
    private String orderItemId;
    @Column(nullable = false)
    private String orderId;
    @Column(nullable = false)
    private String productId;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private double lineTotal;

    protected OrderItem() {
    }

    private OrderItem(OrderItemBuilder builder) {
        this.orderItemId = builder.orderItemId;
        this.orderId = builder.orderId;
        this.productId = builder.productId;
        this.quantity = builder.quantity;
        this.lineTotal = builder.lineTotal;
    }

    public String getOrderItemId() {
        return orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public static class OrderItemBuilder {
        private String orderItemId;
        private String orderId;
        private String productId;
        private int quantity;
        private double lineTotal;

        public OrderItemBuilder setOrderItemId(String orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }

        public OrderItemBuilder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderItemBuilder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public OrderItemBuilder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public OrderItemBuilder setLineTotal(double lineTotal) {
            this.lineTotal = lineTotal;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }

    @Override
    public String toString() {
        return "OrderItemService{" + "orderItemId='" + orderItemId + '\'' + ", orderId='" + orderId + '\'' + ", productId='" + productId + '\'' + ", quantity=" + quantity + ", lineTotal=" + lineTotal + '}';
    }
}
