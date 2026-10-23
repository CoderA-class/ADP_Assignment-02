package za.ac.cput.adp_assignment02.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        @Column(name = "order_id", nullable = false)
    private String orderId;
        @ManyToOne
        @JoinColumn(name = "order_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_order"))
        @JsonIgnore
        private Order order;
        @Column(name = "product_id", nullable = false)
    private String productId;
        @ManyToOne
        @JoinColumn(name = "product_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_product"))
        @JsonIgnore
        private Product product;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private double lineTotal;

    public OrderItem() {
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

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(double lineTotal) {
        this.lineTotal = lineTotal;
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
