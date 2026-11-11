package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Payment;

import java.util.List;

/*
 * PaymentRepository.java
 * Stores payments made against account (pay-later) orders.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderIdOrderByPaidAtDesc(String orderId);

    List<Payment> findByCustomerIdOrderByPaidAtDesc(String customerId);

    List<Payment> findAllByOrderByPaidAtDesc();
}
