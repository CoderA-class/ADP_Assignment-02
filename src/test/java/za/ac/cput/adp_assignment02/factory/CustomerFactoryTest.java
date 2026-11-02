package za.ac.cput.adp_assignment02.factory;

/*
 * CustomerFactoryTest.java
 * Author: David Daniel Sepkitt (240046935)
 */

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Customer;

import static org.junit.jupiter.api.Assertions.*;

class CustomerFactoryTest {

    @Test
    void createCustomer() {
        Customer c = CustomerFactory.createCustomer("CUS-0001", "Megan", "Adams", "megan@adamscafe.co.za", "082 444 5566", "7 Kloof St");
        assertNotNull(c);
        assertEquals("CUS-0001", c.getCustID());
        assertEquals("Megan", c.getFirstName());
        assertEquals("Adams", c.getLastName());
        assertEquals("megan@adamscafe.co.za", c.getEmail());
        assertEquals("082 444 5566", c.getPhoneNumber());
        assertEquals("7 Kloof St", c.getAddress());
    }
}
