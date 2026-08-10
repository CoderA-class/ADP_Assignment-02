package za.ac.cput.adp_assignment02.repository;

/* * OrderItemRepository.java
* OrderItemService Repository
* Author: Adriaan van der Westhuizen (240436415)
* Date: 10 August 2026
*/

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.OrderItem;


public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}
