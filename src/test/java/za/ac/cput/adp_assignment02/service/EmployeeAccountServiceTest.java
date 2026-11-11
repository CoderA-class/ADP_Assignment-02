package za.ac.cput.adp_assignment02.service;

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
import static org.mockito.Mockito.*;

/*
 * EmployeeAccountServiceTest.java
 * Ban / unban and admin password resets.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeAccountServiceTest {

    @Mock private EmployeeRepository repository;
    @Mock private IdGeneratorService idGenerator;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private EmployeeService service;
    private Employee lerato;
    private Employee admin;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(repository, idGenerator, encoder);
        lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").setEmail("lerato@wholesale.local")
                .setPassword(encoder.encode("Staff@123")).setEmpStatus(Employee.EmpStatus.ACTIVE).setAccessLevel(Employee.AccessLevel.EMPLOYEE).build();
        admin = new Employee.Builder().setEmpId("EMP-0001").setFName("Sam").setLName("Admin").setEmail("admin@wholesale.local")
                .setPassword(encoder.encode("Admin@123")).setEmpStatus(Employee.EmpStatus.ACTIVE).setAccessLevel(Employee.AccessLevel.ADMIN).build();
    }

    @Test
    void banningSuspendsTheAccountAndUnbanningRestoresIt() {
        when(repository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Employee.EmpStatus.SUSPENDED, service.ban("EMP-0002", "EMP-0001").getEmpStatus());
        assertEquals(Employee.EmpStatus.ACTIVE, service.unban("EMP-0002").getEmpStatus());
    }

    @Test
    void adminsCannotBanThemselvesOrTheLastAdmin() {
        when(repository.findById("EMP-0001")).thenReturn(Optional.of(admin));
        assertThrows(IllegalStateException.class, () -> service.ban("EMP-0001", "EMP-0001"));

        when(repository.findAll()).thenReturn(List.of(admin, lerato));
        assertThrows(IllegalStateException.class, () -> service.ban("EMP-0001", "EMP-0009"));
    }

    @Test
    void resetGivesAWorkingTemporaryPasswordThatMustBeChanged() {
        when(repository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        String temporary = service.resetPassword("EMP-0002");

        assertTrue(temporary.matches("Nexus-\\d{4}[A-Z]{2}"));
        assertTrue(encoder.matches(temporary, lerato.getPassword()));
        assertTrue(lerato.isMustChangePassword());

        service.changePassword("EMP-0002", temporary, "MyNewPass1");
        assertTrue(encoder.matches("MyNewPass1", lerato.getPassword()));
        assertFalse(lerato.isMustChangePassword());
    }

    @Test
    void theNewPasswordMustBeDifferent() {
        when(repository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        assertThrows(IllegalArgumentException.class, () -> service.changePassword("EMP-0002", "Staff@123", "Staff@123"));
    }
}
