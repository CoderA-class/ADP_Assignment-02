package factory;

/*
  CustomerFactory.java
  CustomerFactory model class
  Author: David Daniel Sepkitt(240046935)
  Date: 19 March 2026
 */

import domain.Customer;


public class CustomerFactory {

    public static Customer createCustomer(
            String custID,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String address
    ) {
        return new Customer.Builder()
                .setCustID(custID)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setAddress(address)
                .build();
    }

}