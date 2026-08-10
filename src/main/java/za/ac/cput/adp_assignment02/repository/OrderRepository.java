package za.ac.cput.adp_assignment02.repository;

/* * OrderRepository.java
* Order Repository
* Author: Rocco Given Visagie (220343527)
* Date: 10 August 2026
*/

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Order;


public interface OrderRepository extends JpaRepository<Order, String> {
}