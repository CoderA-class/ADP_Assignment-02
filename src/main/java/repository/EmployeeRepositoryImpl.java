package repository;

import domain.Employee;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final Map<String, Employee> employeeMap = new HashMap<>();

    @Override
    public Employee create(Employee employee) {
        employeeMap.put(employee.getEmpID(), employee);
        return employee;
    }

    @Override
    public Employee read(String empID) {
        return employeeMap.get(empID);
    }

    @Override
    public Employee update(Employee employee) {
        if (employeeMap.containsKey(employee.getEmpID())) {
            employeeMap.put(employee.getEmpID(), employee);
            return employee;
        }
        return null;
    }

    @Override
    public boolean delete(String empID) {
        return employeeMap.remove(empID) != null;
    }

    @Override
    public Set<Employee> getAll() {
        return new HashSet<>(employeeMap.values());
    }
}