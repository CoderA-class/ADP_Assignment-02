package za.ac.cput.adp_assignment02.domain;

/*
 * EmployeeTest.java
 * Tests for the Employee domain class
 * Author: Robyn Dominique Stevens (222201789)
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee.Builder()
                .setEmpId("EMP-0001")
                .setFName("Lerato")
                .setLName("Dlamini")
                .setSalary(14500)
                .setEmail("lerato@wholesale.local")
                .setPhone("074 123 4567")
                .setRole("Sales Assistant")
                .setEmpStatus(Employee.EmpStatus.ACTIVE)
                .setAccessLevel(Employee.AccessLevel.EMPLOYEE)
                .build();
    }

    @Test
    void builderSetsEveryField() {
        assertEquals("EMP-0001", employee.getEmpID());
        assertEquals("Lerato", employee.getFName());
        assertEquals("Dlamini", employee.getLName());
        assertEquals(14500, employee.getSalary());
        assertEquals("lerato@wholesale.local", employee.getEmail());
        assertEquals("074 123 4567", employee.getPhone());
        assertEquals("Sales Assistant", employee.getRole());
        assertEquals(Employee.EmpStatus.ACTIVE, employee.getEmpStatus());
        assertEquals(Employee.AccessLevel.EMPLOYEE, employee.getAccessLevel());
    }

    @Test
    void accessLevelDefaultsToEmployee() {
        Employee noAccessLevel = new Employee.Builder().setEmpId("EMP-0002").setFName("Test").build();
        assertEquals(Employee.AccessLevel.EMPLOYEE, noAccessLevel.getAccessLevel());
    }

    @Test
    void copyCreatesAnEqualEmployee() {
        Employee copy = new Employee.Builder().copy(employee).setRole("Cashier").build();
        assertEquals(employee, copy);
        assertEquals("Cashier", copy.getRole());
        assertEquals("Lerato", copy.getFName());
    }

    @Test
    void fullNameJoinsFirstAndLastName() {
        assertEquals("Lerato Dlamini", employee.getFullName());
    }

    @Test
    void employeesWithDifferentIdsAreNotEqual() {
        Employee other = new Employee.Builder().copy(employee).setEmpId("EMP-0009").build();
        assertNotEquals(employee, other);
    }

    @Test
    void toStringContainsTheId() {
        assertTrue(employee.toString().contains("EMP-0001"));
    }
}
