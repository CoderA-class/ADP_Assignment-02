package za.ac.cput.adp_assignment02.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Customer;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerFactoryTest {
    @Test
    void createsCustomerWithProvidedDetails() {
        Customer customer = CustomerFactory.createCustomer(
                "C-1", "Ava", "Mokoena", "ava@example.com", "0821234567", "Cape Town");

        assertEquals("C-1", customer.getCustID());
        assertEquals("Ava", customer.getFirstName());
        assertEquals("Mokoena", customer.getLastName());
        assertEquals("ava@example.com", customer.getEmail());
        assertEquals("0821234567", customer.getPhoneNumber());
        assertEquals("Cape Town", customer.getAddress());
    }
}