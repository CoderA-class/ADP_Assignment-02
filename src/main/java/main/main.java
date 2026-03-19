package main;

import domain.Customer;
import repository.*;
import factory.*;

public class main {
        public static void main(String[] args) {

            // Get the repository instance (singleton)
            var customerRepo = CustomerRepositoryImpl.getRepository();

            // Create some customers using the factory
            Customer c1 = factory.CustomerFactory.createCustomer(
                    "C0010", "Jason", "Person",
                    "jasonP@gmail.com", "0831234567",
                    "72 Cool Street");

            Customer c2 = factory.CustomerFactory.createCustomer(
                    "C1020", "Grayson", "Smith",
                    "ghost@gmail.com", "0839561111",
                    "256 Somewhere street");

            Customer c3 = factory.CustomerFactory.createCustomer(
                    "C0012", "Joshua", "Brown",
                    "JoshuaB@gmail.com", "0279871112",
                    "78 Faraway Avenue");

            customerRepo.create(c1);
            customerRepo.create(c2);
            customerRepo.create(c3);

            System.out.println("All Customers:");
            customerRepo.getAll().forEach(System.out::println);
        }
    }

