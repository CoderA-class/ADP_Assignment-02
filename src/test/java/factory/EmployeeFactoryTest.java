package za.ac.cput.adp_assignment02.factory;

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Employee;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeFactoryTest {
    @Test
    void createsEmployeeWithActiveStatusAndUniqueId() {
        Employee first = EmployeeFactory.createEmployee(
                "Mia", "Dlamini", 42000.50, "mia@example.com", "0821234567", "Analyst");
        Employee second = EmployeeFactory.createEmployee(
                "Noah", "Nkosi", 39000.00, "noah@example.com", "0827654321", "Clerk");

        assertTrue(first.getEmpID().matches("EMP\\d{4}"));
        assertNotEquals(first.getEmpID(), second.getEmpID());
        assertEquals("Mia", first.getFName());
        assertEquals("Dlamini", first.getLName());
        assertEquals(42000.50, first.getSalary());
        assertEquals("mia@example.com", first.getEmail());
        assertEquals("0821234567", first.getPhone());
        assertEquals("Analyst", first.getRole());
        assertEquals(Employee.EmpStatus.ACTIVE, first.getEmpStatus());
    }
}