package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/*
* Supplier.java
* Supplier model class
* Author: Luke John Zyster (220489114)
* Date: 25 March 2026
* Updated: auto-generated supplier ID (SUP-0001), contact person and optional image.
*/

@Entity
@Table(name = "supplier")
public class Supplier {
    @Id
    @Column(name = "supplierId")
    private String supplierId;
    @Column(nullable = false)
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    public Supplier() {
    }

    private Supplier(SupplierBuilder builder) {
        this.supplierId = builder.supplierId;
        this.name = builder.name;
        this.contactPerson = builder.contactPerson;
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.imageUrl = builder.imageUrl;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static class SupplierBuilder {
        private String supplierId;
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String address;
        private String imageUrl;

        public SupplierBuilder setSupplierId(String supplierId) {
            this.supplierId = supplierId;
            return this;
        }

        public SupplierBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public SupplierBuilder setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
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

        public SupplierBuilder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
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
