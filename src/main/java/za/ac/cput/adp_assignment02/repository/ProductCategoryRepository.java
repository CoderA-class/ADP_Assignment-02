package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.ProductCategory;

import java.util.List;
import java.util.Optional;

/*
 * ProductCategoryRepository.java
 * Stores the stock categories the admin manages.
 */
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    Optional<ProductCategory> findByNameIgnoreCase(String name);

    List<ProductCategory> findAllByOrderByNameAsc();
}
