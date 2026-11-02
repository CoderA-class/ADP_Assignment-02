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
}
