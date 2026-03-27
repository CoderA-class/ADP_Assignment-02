package domain;

/*
  Product.java
  Product model class
  Author: Isaac Tinotenda Ziyengwa(231269307)
  Date: 26 March 2026
 */


import java.lang.module.ModuleDescriptor;

public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String category;

    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.category = builder.category;
    }

    public Product() {
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
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

    public static class Builder {
        private String id;
        private String name;
        private double price;
        private int quantity;
        private String category;

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

        public Builder copy(Product product) {
            this.id = product.id;
            this.name = product.name;
            this.price = product.price;
            this.quantity = product.quantity;
            this.category = product.category;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
