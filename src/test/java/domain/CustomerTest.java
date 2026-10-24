package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerTest {
    @Test
    void builderCreatesCustomerWithAllFields() {
        Customer customer = new Customer.Builder()
                .setCustID("C-1")
                .setFirstName("Ava")
                .setLastName("Mokoena")
                .setEmail("ava@example.com")
                .setPhoneNumber("0821234567")
                .setAddress("Cape Town")
                .build();

        assertNotNull(customer);
        assertEquals("C-1", customer.getCustID());
        assertEquals("Ava", customer.getFirstName());
        assertEquals("Mokoena", customer.getLastName());
        assertEquals("ava@example.com", customer.getEmail());
        assertEquals("0821234567", customer.getPhoneNumber());
        assertEquals("Cape Town", customer.getAddress());
    }
}