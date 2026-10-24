package za.ac.cput.adp_assignment02.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.ac.cput.adp_assignment02.domain.Employee;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository repository;

    @Test
    void persistsEmployeeAndReadsItById() {
        Employee employee = new Employee.Builder().setEmpId("E-1").setFName("Mia")
                .setLName("Dlamini").setSalary(42000.50).setEmail("mia@example.com")
                .setPhone("0821234567").setRole("Analyst")
                .setEmpStatus(Employee.EmpStatus.ACTIVE).build();

        repository.save(employee);

        Employee saved = repository.findById("E-1").orElseThrow();
        assertEquals("Mia", saved.getFName());
        assertEquals(Employee.EmpStatus.ACTIVE, saved.getEmpStatus());
        assertTrue(repository.existsById("E-1"));
    }
}