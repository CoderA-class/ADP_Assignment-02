package za.ac.cput.adp_assignment02.repository;

/* * ProductRepository.java
* Product Repository
* Author: Isaac Tinotenda Ziyengwa (231269307)
* Date: 10 August 2026
*/

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Product;


import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByNameIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByQuantityLessThan(int threshold);

    boolean existsByNameIgnoreCase(String name);
}