package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/* * Customer.java *
Customer model class
* Author: David Daniel Sepkitt (240046935)
* Date: 10 August 2026
*/

@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @Column(name = "custID")
    private String custID;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String address;

    public Customer() {
    }


    private Customer(Builder builder) {
        this.custID = builder.custID;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.phoneNumber = builder.phoneNumber;
        this.address = builder.address;
    }

    public String getCustID() {
        return custID;
    }

    public void setCustID(String custID) {
        this.custID = custID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" + "Customer ID='" + custID + '\'' + ", First name='" + firstName + '\'' + ", Last name='" + lastName + '\'' + ", Email address='" + email + '\'' + ", Phone number='" + phoneNumber + '\'' + ", Address='" + address + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customer)) {
            return false;
        }
        Customer customer = (Customer) o;
        return Objects.equals(custID, customer.custID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(custID);
    }

    public static class Builder {
        private String custID;
        private String firstName;
        private String lastName;
        private String email;
        private String phoneNumber;
        private String address;

        public Builder setCustID(String custID) {
            this.custID = custID;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setAddress(String address) {
            this.address = address;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}