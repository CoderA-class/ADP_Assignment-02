package domain;

/*
  Customer.java
  Customer model class
  Authorad: David Daniel Sepkitt(240046935)
  Date: 19 March 2026
 */


import java.util.Objects;

public class Customer {

    private final String custID;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phoneNumber;
    private final String address;

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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "Customer ID='" + custID + '\'' +
                ", First name='" + firstName + '\'' +
                ", Last name='" + lastName + '\'' +
                ", Email address='" + email + '\'' +
                ", Phone number='" + phoneNumber + '\'' +
                ", Address='" + address + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
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