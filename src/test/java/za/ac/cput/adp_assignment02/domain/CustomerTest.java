package za.ac.cput.adp_assignment02.domain;

/*
 * CustomerTest.java
 * Tests for the Customer domain class
 * Author: David Daniel Sepkitt (240046935)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer.Builder()
                .setCustID("CUS-0001")
                .setFirstName("Sipho")
                .setLastName("Ndlovu")
                .setEmail("sipho@ndlovuspaza.co.za")
                .setPhoneNumber("072 111 2233")
                .setAddress("45 Long St, Khayelitsha")
                .setCompany("Ndlovu Spaza Shop")
                .setCreatedDate(LocalDate.of(2026, 1, 15))
                .build();
    }

    @Test
    void builderSetsEveryField() {
        assertEquals("CUS-0001", customer.getCustID());
        assertEquals("Sipho", customer.getFirstName());
        assertEquals("Ndlovu", customer.getLastName());
        assertEquals("sipho@ndlovuspaza.co.za", customer.getEmail());
        assertEquals("072 111 2233", customer.getPhoneNumber());
        assertEquals("45 Long St, Khayelitsha", customer.getAddress());
        assertEquals("Ndlovu Spaza Shop", customer.getCompany());
        assertEquals(LocalDate.of(2026, 1, 15), customer.getCreatedDate());
        assertNull(customer.getLastPurchaseDate());
    }

    @Test
    void fullNameJoinsFirstAndLastName() {
        assertEquals("Sipho Ndlovu", customer.getFullName());
    }

    @Test
    void fullNameWorksWithOnlyAFirstName() {
        Customer onlyFirst = new Customer.Builder().setFirstName("Sipho").build();
        assertEquals("Sipho", onlyFirst.getFullName());
    }

    @Test
    void customersWithTheSameIdAreEqual() {
        Customer sameId = new Customer.Builder().setCustID("CUS-0001").setFirstName("Someone").build();
        Customer otherId = new Customer.Builder().setCustID("CUS-0002").setFirstName("Sipho").build();
        assertEquals(customer, sameId);
        assertEquals(customer.hashCode(), sameId.hashCode());
        assertNotEquals(customer, otherId);
    }

    @Test
    void settersUpdateValues() {
        customer.setLastPurchaseDate(LocalDate.of(2026, 11, 2));
        customer.setPhoneNumber("082 000 0000");
        assertEquals(LocalDate.of(2026, 11, 2), customer.getLastPurchaseDate());
        assertEquals("082 000 0000", customer.getPhoneNumber());
    }

    @Test
    void toStringContainsTheId() {
        assertTrue(customer.toString().contains("CUS-0001"));
    }
}
