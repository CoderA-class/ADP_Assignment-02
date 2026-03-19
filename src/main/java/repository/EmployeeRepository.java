package repository;

import domain.Employee;

import java.util.Set;

public interface EmployeeRepository {

    Employee create(Employee employee);

    Employee read(String empID);

    Employee update(Employee employee);

    boolean delete(String empID);

    Set<Employee> getAll();
}