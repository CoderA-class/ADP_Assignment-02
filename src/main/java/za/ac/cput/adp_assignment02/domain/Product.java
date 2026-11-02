package za.ac.cput.adp_assignment02.domain;

/*
  Product.java
  Product model class
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 26 March 2026
  Updated: auto-generated product ID (PRD-0001), image, description, supplier link
           and a re-order level used for low-stock notifications.
 */

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    public static final int DEFAULT_REORDER_LEVEL = 10;

    @Id
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String category;
    private String description;
    private String supplierId;
    private Integer reorderLevel;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.category = builder.category;
        this.description = builder.description;
        this.supplierId = builder.supplierId;
        this.reorderLevel = builder.reorderLevel;
        this.imageUrl = builder.imageUrl;
    }

    public Product() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public int getReorderLevel() {
        return reorderLevel == null ? DEFAULT_REORDER_LEVEL : reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLowStock() {
        return quantity <= getReorderLevel();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", category='" + category + '\'' +
                '}';
    }

    // Builder Pattern
    public static class Builder {
        private String id;
        private String name;
        private double price;
        private int quantity;
        private String category;
        private String description;
        private String supplierId;
        private Integer reorderLevel;
        private String imageUrl;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setSupplierId(String supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public Builder setReorderLevel(Integer reorderLevel) {
            this.reorderLevel = reorderLevel;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder copy(Product product) {
            this.id = product.id;
            this.name = product.name;
            this.price = product.price;
            this.quantity = product.quantity;
            this.category = product.category;
            this.description = product.description;
            this.supplierId = product.supplierId;
            this.reorderLevel = product.reorderLevel;
            this.imageUrl = product.imageUrl;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
