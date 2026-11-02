package za.ac.cput.adp_assignment02.repository;

/*
* EmployeeRepository.java
* Employee Repository
* Author: Robyn Dominique Stevens (222201789)
* Date: 10 August 2026
*/

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.adp_assignment02.domain.Employee;

import java.util.List;
import java.util.Optional;


public interface EmployeeRepository extends JpaRepository<Employee, String> {

    @Query("select e from Employee e where lower(concat(coalesce(e.fName,''), ' ', coalesce(e.lName,''))) like lower(concat('%', :name, '%')) order by e.fName, e.lName")
    List<Employee> searchByName(@Param("name") String name);

    @Query("select e from Employee e where lower(e.email) = lower(:email)")
    Optional<Employee> findByEmailIgnoreCase(@Param("email") String email);

    @Query("select count(e) from Employee e where e.accessLevel = :level")
    long countByAccessLevel(@Param("level") Employee.AccessLevel level);
}
