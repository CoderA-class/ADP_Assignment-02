package za.ac.cput.adp_assignment02.service;

/*
EmployeeService.java
Employee Service Implementation
Author: Robyn Dominique Stevens (222201789)
Date: 10 August 2026
*/


import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;


import java.util.List;

@Service
public class EmployeeService implements IEmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Employee create(Employee employee) {
        if (repository.existsById(employee.getEmpID())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee ID already exists.");
        }
        return repository.save(employee);
    }

    @Override
    public Employee read(String empID) {
        return repository.findById(empID).orElse(null);
    }

    @Override
    public Employee update(Employee employee) {
        return repository.save(employee);
    }

    @Override
    public boolean delete(String empID) {
        if (repository.existsById(empID)) {
            repository.deleteById(empID);
            return true;
        }
        return false;
    }

    @Override
    public List<Employee> getAll() {
        return repository.findAll();
    }
}
