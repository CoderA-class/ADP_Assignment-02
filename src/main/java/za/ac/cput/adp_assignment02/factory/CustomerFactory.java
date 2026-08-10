package za.ac.cput.adp_assignment02.factory;

/*
CustomerFactory.java
Customer Factory class
Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/


import za.ac.cput.adp_assignment02.domain.Customer;

public class CustomerFactory {
    public static Customer createCustomer(String custID, String firstName, String lastName, String email, String phoneNumber, String address) {
        return new Customer.Builder().setCustID(custID).setFirstName(firstName).setLastName(lastName).setEmail(email).setPhoneNumber(phoneNumber).setAddress(address).build();
    }
}
