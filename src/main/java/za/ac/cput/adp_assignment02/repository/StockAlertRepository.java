package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.StockAlert;

import java.util.Collection;
import java.util.List;

/*
 * StockAlertRepository.java
 * Stores staff stock alerts and finds the open ones for a product.
 */
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {

    List<StockAlert> findAllByOrderByCreatedAtDesc();

    List<StockAlert> findByProductIdAndStatusIn(String productId, Collection<StockAlert.Status> statuses);
}
