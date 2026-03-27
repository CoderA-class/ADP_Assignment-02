package repository;

/*
        EmployeeRepository.java
        EmployeeRepository model class
        Author: Robyn Dominique Stevens (222201789)
        Date: 19 March 2026
        */

import domain.Employee;

import java.util.Set;

public interface EmployeeRepository {

    Employee create(Employee employee);

    Employee read(String empID);

    Employee update(Employee employee);

    boolean delete(String empID);

    Set<Employee> getAll();
}