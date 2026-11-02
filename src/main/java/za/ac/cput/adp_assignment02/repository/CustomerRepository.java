package za.ac.cput.adp_assignment02.repository;

/*
 * CustomerRepository.java
 * Customer Repository
 * Author: David Daniel Sepkitt (240046935)
 * Date: 10 August 2026
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.adp_assignment02.domain.Customer;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    /* Search by first name, last name or full name (case-insensitive, partial match) */
    @Query("select c from Customer c where lower(concat(coalesce(c.firstName,''), ' ', coalesce(c.lastName,''))) like lower(concat('%', :name, '%')) "
            + "or lower(coalesce(c.company,'')) like lower(concat('%', :name, '%')) order by c.firstName, c.lastName")
    List<Customer> searchByName(@Param("name") String name);
}
