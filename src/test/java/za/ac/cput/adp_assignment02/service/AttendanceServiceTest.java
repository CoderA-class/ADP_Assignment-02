package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Attendance;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.repository.AttendanceRepository;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * AttendanceServiceTest.java
 * Clocking in and out, and the rules around open shifts.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository repository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private NotificationService notificationService;

    private AttendanceService service;
    private final Employee lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();

    @BeforeEach
    void setUp() {
        service = new AttendanceService(repository, employeeRepository, notificationService);
    }

    @Test
    void clockingInStartsAShiftAndTellsTheAdmins() {
        when(employeeRepository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        when(repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc("EMP-0002")).thenReturn(Optional.empty());
        when(repository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        Attendance shift = service.clockIn("EMP-0002");

        assertEquals("Lerato Dlamini", shift.getEmployeeName());
        assertNull(shift.getClockOut());
        verify(notificationService).notify(eq(Notification.Type.CLOCK_IN), anyString(), anyString(), eq("EMP-0002"), eq(Notification.AUDIENCE_ADMIN));
    }

    @Test
    void youCannotClockInTwice() {
        when(employeeRepository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        when(repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc("EMP-0002"))
                .thenReturn(Optional.of(new Attendance("EMP-0002", "Lerato Dlamini", LocalDateTime.now())));

        assertThrows(IllegalStateException.class, () -> service.clockIn("EMP-0002"));
    }

    @Test
    void clockingOutClosesTheOpenShift() {
        Attendance open = new Attendance("EMP-0002", "Lerato Dlamini", LocalDateTime.now().minusHours(8));
        when(repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc("EMP-0002")).thenReturn(Optional.of(open));
        when(repository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        Attendance done = service.clockOut("EMP-0002");

        assertNotNull(done.getClockOut());
        assertTrue(done.getMinutesWorked() >= 479);
        verify(notificationService).notify(eq(Notification.Type.CLOCK_OUT), anyString(), anyString(), eq("EMP-0002"), eq(Notification.AUDIENCE_ADMIN));
    }

    @Test
    void clockingOutWithoutAShiftIsRefused() {
        when(repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc("EMP-0002")).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> service.clockOut("EMP-0002"));
        assertNull(service.currentShift("EMP-0002"));
    }
}
