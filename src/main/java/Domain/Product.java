/*
  Product.java
  Product model class
  Author: Isaac Tinotenda Ziyengwa (231269307)
  Date: 26 March 2026
 */

package Domain;

public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private String category;


    public Product(String id, String name, double price, int quantity, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }


    private Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.category = builder.category;
    }

    public Product() {
    }


    public String getId() {
        return id;
    }



    public String getName() {
        return name;
    }



    public double getPrice() {
        return price;
    }


    public int getQuantity() {
        return quantity;
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

    // Builder Pattern
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