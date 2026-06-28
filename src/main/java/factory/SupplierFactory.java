package factory;

/*
  SupplierFactory.java
  SupplierFactory model class
  Author: Luke John Zyster (220489114)
  Date: 25 March 2026
 */

import domain.Supplier;

public class SupplierFactory {
    public static Supplier createSupplier(String supplierId, String name, String email, String phone, String address) {
        if (supplierId == null || supplierId.isEmpty())
            throw new IllegalArgumentException("supplierId is required");

        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("name is required");

        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("email is required");

        if (phone == null || phone.isEmpty())
            throw new IllegalArgumentException("phone is required");

        if (address == null || address.isEmpty())
            throw new IllegalArgumentException("address is required");

        return new Supplier.SupplierBuilder()
                .setSupplierId(supplierId)
                .setName(name)
                .setEmail(email)
                .setPhone(phone)
                .setAddress(address)
                .build();

    }
}
