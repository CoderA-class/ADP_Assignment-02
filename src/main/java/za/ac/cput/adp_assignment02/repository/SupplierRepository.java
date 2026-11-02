package za.ac.cput.adp_assignment02.repository;

/* * SupplierRepository.java * Supplier Repository * Author: Luke John Zyster (220489114) * Date: 10 August 2026 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.ac.cput.adp_assignment02.domain.Supplier;

import java.util.List;


public interface SupplierRepository extends JpaRepository<Supplier, String> {

    @Query("select s from Supplier s where lower(s.name) like lower(concat('%', :name, '%')) "
            + "or lower(coalesce(s.contactPerson,'')) like lower(concat('%', :name, '%')) order by s.name")
    List<Supplier> searchByName(@Param("name") String name);
}
