package domain;

/*
  main.java
  Author: Adriaan van der Westhuizen (240436415)
  Date: 26 March 2026
 */

import factory.OrderItemFactory;

import static domain.Employee.EmpStatus.ACTIVE;

public class main {
    public static void main(String[] args) {


        Customer c1 = new Customer.Builder()
                .setFirstName("Adriaan")
                .setLastName("vdw")
                .setAddress(" 22 Church Street")
                .setCustID("C1102")
                .setEmail("adriaanvdw@gmail.com")
                .setPhoneNumber("0832361787")
                .build();

        System.out.println(c1);

        Employee emp1 = new Employee.Builder()
                .setFName("Rocco")
                .setLName("Visagie")
                .setEmpId("emp006")
                .setRole("shelf stacker")
                .setEmpStatus(ACTIVE)
                .setEmail("roccovisagie@gmail.com")
                .setSalary(20000.00)
                .build();

        Order o1 = new Order.Builder()
                .setOrderId("ORD777")
                .setCustomerId("C1102")
                .setTotalAmount(234.00)
                .addOrderItem(OrderItemFactory.createOrderItem("OI5000", "ORD777", "PROD100", 20, 4500.00))
                .build();


        OrderItem orderItem = OrderItemFactory.createOrderItem(
                "OI5000",
                "ORD777",
                "PROD100",
                20,
                4500.00
        );

        Supplier s1 = new Supplier.SupplierBuilder()
                .setName("Woolworths")
                .setAddress("12 Pelecan Street")
                .setEmail("WW@ww.co.za")
                .setPhone("083 747 8291")
                .build();

        System.out.println("\nCreated OrderItem:");
        System.out.println(orderItem);


        OrderItemRepositoryImpl repository = OrderItemRepositoryImpl.getRepository();


        repository.create(orderItem);
        System.out.println("\nOrderItem saved to repository.");


        OrderItem readItem = repository.read("OI5000");
        System.out.println("\nRead from repository:");
        System.out.println(readItem);


        OrderItem updatedOrderItem = new OrderItem.OrderItemBuilder()
                .setOrderItemId("OI5000")
                .setOrderId("ORD777")
                .setProductId("PROD100")
                .setQuantity(30)
                .setLineTotal(6000.00)
                .build();

        repository.update(updatedOrderItem);

        System.out.println("\nUpdated OrderItem:");
        System.out.println(repository.read("OI5000"));


        boolean deleted = repository.delete("OI5000");
        System.out.println("\nDeleted OrderItem: " + deleted);


        System.out.println("\nTrying to read deleted item:");
        System.out.println(repository.read("OI5000"));
    }
}