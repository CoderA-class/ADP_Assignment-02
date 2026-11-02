package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmployeeTest {
    @Test
    void builderCreatesEmployeeWithStatusAndSalary() {
        Employee employee = new Employee.Builder()
                .setEmpId("E-1")
                .setFName("Mia")
                .setLName("Dlamini")
                .setSalary(42000.50)
                .setEmail("mia@example.com")
                .setPhone("0821234567")
                .setRole("Analyst")
                .setEmpStatus(Employee.EmpStatus.SUSPENDED)
                .build();

        assertNotNull(employee);
        assertEquals("E-1", employee.getEmpID());
        assertEquals("Mia", employee.getFName());
        assertEquals("Dlamini", employee.getLName());
        assertEquals(42000.50, employee.getSalary());
        assertEquals("mia@example.com", employee.getEmail());
        assertEquals("0821234567", employee.getPhone());
        assertEquals("Analyst", employee.getRole());
        assertEquals(Employee.EmpStatus.SUSPENDED, employee.getEmpStatus());
    }
}