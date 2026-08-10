package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
* Supplier.java
* Supplier model class
* Author: Luke John Zyster (220489114)
* Date: 25 March 2026
*/

@Entity
@Table(name = "supplier")
public class Supplier {
    @Id
    @Column(name = "supplierId")
    private String supplierId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String address;

    protected Supplier() {
    }

    private Supplier(SupplierBuilder builder) {
        this.supplierId = builder.supplierId;
        this.name = builder.name;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public static class SupplierBuilder {
        private String supplierId;
        private String name;
        private String email;
        private String phone;
        private String address;

        public SupplierBuilder setSupplierId(String supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public SupplierBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public SupplierBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public SupplierBuilder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public SupplierBuilder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Supplier build() {
            return new Supplier(this);
        }
    }

    @Override
    public String toString() {
        return "Supplier{" + "supplierId = '" + supplierId + '\'' + ", name = '" + name + '\'' + ", email = '" + email + '\'' + ", phone = '" + phone + '\'' + ", address = '" + address + '\'' + '}';
    }
}
