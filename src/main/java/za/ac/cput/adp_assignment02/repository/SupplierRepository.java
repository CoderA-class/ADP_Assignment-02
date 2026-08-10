package za.ac.cput.adp_assignment02.repository;

/* * SupplierRepository.java * Supplier Repository * Author: Luke John Zyster (220489114) * Date: 10 August 2026 */

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Supplier;


public interface SupplierRepository extends JpaRepository<Supplier, String> {
}
