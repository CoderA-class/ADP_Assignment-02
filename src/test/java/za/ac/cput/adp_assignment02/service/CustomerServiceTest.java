package za.ac.cput.adp_assignment02.service;

/*
 * CustomerServiceTest.java
 * Author: David Daniel Sepkitt (240046935)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;
    @Mock
    private IdGeneratorService idGenerator;

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(repository, idGenerator);
    }

    @Test
    void createNeedsOnlyANameAndGeneratesTheId() {
        when(idGenerator.nextId(eq(IdGeneratorService.CUSTOMER), any())).thenReturn("CUS-0007");
        when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer saved = service.create(new Customer.Builder().setFirstName(" Fatima ").build());

        assertEquals("CUS-0007", saved.getCustID());
        assertEquals("Fatima", saved.getFirstName());
        assertEquals("", saved.getLastName());
        assertEquals("", saved.getEmail());
        assertEquals(LocalDate.now(), saved.getCreatedDate());
    }

    @Test
    void createWithoutANameFails() {
        assertThrows(IllegalArgumentException.class, () -> service.create(new Customer.Builder().setLastName("Patel").build()));
        verify(repository, never()).save(any());
    }

    @Test
    void updateKeepsTheOriginalDatesWhenNotSent() {
        Customer existing = new Customer.Builder().setCustID("CUS-0001").setFirstName("Sipho")
                .setCreatedDate(LocalDate.of(2026, 1, 1)).setLastPurchaseDate(LocalDate.of(2026, 10, 30)).build();
        when(repository.findById("CUS-0001")).thenReturn(Optional.of(existing));
        when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

        Customer saved = service.update(new Customer.Builder().setCustID("CUS-0001").setFirstName("Sipho").setPhoneNumber("071 000 0000").build());

        assertEquals(LocalDate.of(2026, 1, 1), saved.getCreatedDate());
        assertEquals(LocalDate.of(2026, 10, 30), saved.getLastPurchaseDate());
        assertEquals("071 000 0000", saved.getPhoneNumber());
    }

    @Test
    void blankSearchReturnsEveryone() {
        List<Customer> all = List.of(new Customer.Builder().setCustID("CUS-0001").setFirstName("A").build());
        when(repository.findAll()).thenReturn(all);
        assertEquals(all, service.searchByName("  "));
        verify(repository, never()).searchByName(any());
    }

    @Test
    void searchTrimsTheName() {
        when(repository.searchByName("sipho")).thenReturn(List.of());
        service.searchByName("  sipho ");
        verify(repository).searchByName("sipho");
    }
}
