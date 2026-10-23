package za.ac.cput.adp_assignment02.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.ac.cput.adp_assignment02.domain.Employee;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class EmployeeRepositoryImplTest {
    @Autowired
    private EmployeeRepository repository;

    @Test
    void deletesEmployeeFromRepository() {
        Employee employee = new Employee.Builder().setEmpId("E-2").setFName("Noah")
                .setLName("Nkosi").setSalary(39000).setEmail("noah@example.com")
                .setPhone("0827654321").setRole("Clerk")
                .setEmpStatus(Employee.EmpStatus.SUSPENDED).build();
        repository.save(employee);

        repository.deleteById("E-2");

        assertFalse(repository.existsById("E-2"));
    }
}