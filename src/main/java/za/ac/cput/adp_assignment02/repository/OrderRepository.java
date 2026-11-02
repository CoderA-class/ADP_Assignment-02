package za.ac.cput.adp_assignment02.repository;

/* * OrderRepository.java
* Order Repository
* Author: Rocco Given Visagie (220343527)
* Date: 10 August 2026
*/

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.adp_assignment02.domain.Order;

import java.util.List;


public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCustomerIdOrderByOrderDateDesc(String customerId);

    @Query("select o from Order o where lower(coalesce(o.customerName,'')) like lower(concat('%', :name, '%')) order by o.orderDate desc")
    List<Order> searchByCustomerName(@Param("name") String name);
}
