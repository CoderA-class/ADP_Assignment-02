package za.ac.cput.adp_assignment02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.ac.cput.adp_assignment02.domain.Attendance;

import java.util.List;
import java.util.Optional;

/*
 * AttendanceRepository.java
 * Stores clock-in / clock-out shifts and finds an employee's open shift.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc(String employeeId);

    List<Attendance> findByEmployeeIdOrderByClockInDesc(String employeeId);

    List<Attendance> findAllByOrderByClockInDesc();
}
