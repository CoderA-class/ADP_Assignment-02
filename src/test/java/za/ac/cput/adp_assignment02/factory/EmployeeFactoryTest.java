package za.ac.cput.adp_assignment02.factory;

/*
 * EmployeeFactoryTest.java
 * Author: Robyn Dominique Stevens (222201789)
 */

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.Employee;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeFactoryTest {

    @Test
    void createEmployee() {
        Employee e = EmployeeFactory.createEmployee("Lerato", "Dlamini", 14500, "lerato@wholesale.local", "074 123 4567", "Sales Assistant");
        assertNotNull(e);
        assertNotNull(e.getEmpID());
        assertTrue(e.getEmpID().startsWith("EMP"));
        assertEquals("Lerato", e.getFName());
        assertEquals("Dlamini", e.getLName());
        assertEquals(14500, e.getSalary());
        assertEquals("lerato@wholesale.local", e.getEmail());
        assertEquals("074 123 4567", e.getPhone());
        assertEquals("Sales Assistant", e.getRole());
        assertEquals(Employee.EmpStatus.ACTIVE, e.getEmpStatus());
    }

    @Test
    void eachEmployeeGetsAUniqueId() {
        Employee a = EmployeeFactory.createEmployee("A", "One", 1, "a@x.com", "1", "Staff");
        Employee b = EmployeeFactory.createEmployee("B", "Two", 1, "b@x.com", "2", "Staff");
        assertNotEquals(a.getEmpID(), b.getEmpID());
    }

    // ---- team test (moved here from src/test/java/factory) ----

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
