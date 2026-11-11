package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.LeaveRequest;

import java.util.List;

/*
 * LeaveRequestRepository.java
 * Stores leave requests, per employee or all of them.
 */
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    List<LeaveRequest> findByEmployeeIdOrderByRequestedAtDesc(String employeeId);

    List<LeaveRequest> findAllByOrderByRequestedAtDesc();
}
