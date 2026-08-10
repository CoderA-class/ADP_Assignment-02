package za.ac.cput.adp_assignment02.repository;

/*
* EmployeeRepository.java
* Employee Repository
* Author: Robyn Dominique Stevens (222201789)
* Date: 10 August 2026
*/

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Employee;


public interface EmployeeRepository extends JpaRepository<Employee, String> {
}
