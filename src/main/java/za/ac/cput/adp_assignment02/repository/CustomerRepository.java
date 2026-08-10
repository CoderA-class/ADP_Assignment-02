package za.ac.cput.adp_assignment02.repository;

/*
 * CustomerRepository.java
 * Customer Repository
 * Author: David Daniel Sepkitt (240046935)
 * Date: 10 August 2026
 */

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}

