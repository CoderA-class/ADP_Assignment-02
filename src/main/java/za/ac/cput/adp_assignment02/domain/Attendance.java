package za.ac.cput.adp_assignment02.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.LocalDateTime;

/*
 * Attendance.java
 * One clock-in / clock-out shift for an employee.
 */
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String employeeId;
    private String employeeName;
    @Column(nullable = false)
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    public Attendance() {
    }

    public Attendance(String employeeId, String employeeName, LocalDateTime clockIn) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.clockIn = clockIn;
    }

    /* Minutes worked (up to now if still clocked in) */
    public long getMinutesWorked() {
        if (clockIn == null) {
            return 0;
        }
        LocalDateTime end = clockOut == null ? LocalDateTime.now() : clockOut;
        return Duration.between(clockIn, end).toMinutes();
    }

    public boolean isActive() {
        return clockOut == null;
    }

    public Long getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }
}
