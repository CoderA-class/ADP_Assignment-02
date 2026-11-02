package za.ac.cput.adp_assignment02.service;

/*
 * EmployeeServiceTest.java
 * Author: Robyn Dominique Stevens (222201789)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;
    @Mock
    private IdGeneratorService idGenerator;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository, idGenerator, new BCryptPasswordEncoder());
    }

    private Employee newEmployee(String email, String password) {
        return new Employee.Builder().setFName("Lerato").setLName("Dlamini").setEmail(email).setPassword(password).build();
    }

    @Test
    void createGeneratesIdHashesPasswordAndSetsDefaults() {
        when(idGenerator.nextId(eq(IdGeneratorService.EMPLOYEE), any())).thenReturn("EMP-0002");
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee saved = service.create(newEmployee("lerato@wholesale.local", "Secret123"));

        assertEquals("EMP-0002", saved.getEmpID());
        assertNotEquals("Secret123", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"), "password should be a BCrypt hash");
        assertEquals(Employee.EmpStatus.ACTIVE, saved.getEmpStatus());
        assertEquals(Employee.AccessLevel.EMPLOYEE, saved.getAccessLevel());
        assertEquals("Staff", saved.getRole());
        assertNotNull(saved.getHireDate());
    }

    @Test
    void createRequiresNameAndEmail() {
        assertThrows(IllegalArgumentException.class, () -> service.create(newEmployee("", "x")));
        assertThrows(IllegalArgumentException.class, () -> service.create(new Employee.Builder().setEmail("a@b.com").build()));
    }

    @Test
    void createRejectsADuplicateEmail() {
        Employee existing = new Employee.Builder().setEmpId("EMP-0001").setFName("Other").setEmail("lerato@wholesale.local").build();
        when(repository.findByEmailIgnoreCase("lerato@wholesale.local")).thenReturn(Optional.of(existing));
        assertThrows(IllegalStateException.class, () -> service.create(newEmployee("lerato@wholesale.local", "x")));
    }

    @Test
    void authenticateChecksTheHashedPassword() {
        Employee stored = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setEmail("lerato@wholesale.local")
                .setPassword(service.hash("Secret123")).setEmpStatus(Employee.EmpStatus.ACTIVE).build();
        when(repository.findByEmailIgnoreCase("lerato@wholesale.local")).thenReturn(Optional.of(stored));

        assertNotNull(service.authenticate("lerato@wholesale.local", "Secret123"));
        assertNull(service.authenticate("lerato@wholesale.local", "wrong"));
    }

    @Test
    void suspendedEmployeesCannotLogIn() {
        Employee stored = new Employee.Builder().setEmpId("EMP-0003").setFName("Sam").setEmail("sam@wholesale.local")
                .setPassword(service.hash("Secret123")).setEmpStatus(Employee.EmpStatus.SUSPENDED).build();
        when(repository.findByEmailIgnoreCase("sam@wholesale.local")).thenReturn(Optional.of(stored));
        assertThrows(IllegalStateException.class, () -> service.authenticate("sam@wholesale.local", "Secret123"));
    }

    @Test
    void updateKeepsThePasswordWhenLeftBlank() {
        Employee existing = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setEmail("lerato@wholesale.local")
                .setPassword("$2a$existinghash").setEmpStatus(Employee.EmpStatus.ACTIVE).build();
        when(repository.findById("EMP-0002")).thenReturn(Optional.of(existing));
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee changes = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setEmail("lerato@wholesale.local").setPhone("071").build();
        Employee saved = service.update(changes);

        assertEquals("$2a$existinghash", saved.getPassword());
        assertEquals("071", saved.getPhone());
    }

    @Test
    void theOnlyAdminCannotBeDemoted() {
        Employee admin = new Employee.Builder().setEmpId("EMP-0001").setFName("System").setEmail("admin@wholesale.local")
                .setAccessLevel(Employee.AccessLevel.ADMIN).setEmpStatus(Employee.EmpStatus.ACTIVE).setPassword("$2a$x").build();
        when(repository.findById("EMP-0001")).thenReturn(Optional.of(admin));
        when(repository.findAll()).thenReturn(List.of(admin));

        Employee demoted = new Employee.Builder().copy(admin).setAccessLevel(Employee.AccessLevel.EMPLOYEE).build();
        assertThrows(IllegalStateException.class, () -> service.update(demoted));
        assertThrows(IllegalStateException.class, () -> service.delete("EMP-0001"));
    }

    @Test
    void changePasswordNeedsTheCurrentPassword() {
        Employee stored = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setEmail("l@x.com").setPassword(service.hash("OldPass1")).build();
        when(repository.findById("EMP-0002")).thenReturn(Optional.of(stored));

        assertThrows(IllegalArgumentException.class, () -> service.changePassword("EMP-0002", "wrong", "NewPass1"));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword("EMP-0002", "OldPass1", "123"));

        service.changePassword("EMP-0002", "OldPass1", "NewPass1");
        verify(repository).save(stored);
    }
}
