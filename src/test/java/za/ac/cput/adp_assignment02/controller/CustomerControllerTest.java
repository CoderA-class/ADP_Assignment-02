package za.ac.cput.adp_assignment02.controller;

/*
 * CustomerControllerIntegrationTest.java
 * Unit test for CustomerController (services are mocked).
 * Author: David Daniel Sepkitt (240046935)
 */

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.service.CustomerService;
import za.ac.cput.adp_assignment02.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;
    @Mock
    private OrderService orderService;
    @InjectMocks
    private CustomerController controller;

    @Test
    void searchByNameUsesTheService() {
        Customer sipho = new Customer.Builder().setCustID("CUS-0001").setFirstName("Sipho").build();
        when(customerService.searchByName("sip")).thenReturn(List.of(sipho));
        assertEquals(List.of(sipho), controller.search("sip"));
    }

    @Test
    void readUnknownIdReturns404() {
        when(customerService.read("CUS-9999")).thenReturn(null);
        assertEquals(HttpStatus.NOT_FOUND, controller.read("CUS-9999").getStatusCode());
    }
}
