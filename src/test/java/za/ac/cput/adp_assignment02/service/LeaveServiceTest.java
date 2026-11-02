package za.ac.cput.adp_assignment02.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.LeaveRequest;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;
import za.ac.cput.adp_assignment02.repository.LeaveRequestRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * LeaveServiceTest.java
 * Leave requests: request → admin notified, approve/decline → employee notified.
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRequestRepository repository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private IdGeneratorService idGenerator;
    @Mock private NotificationService notificationService;

    private LeaveService service;
    private Employee lerato;

    @BeforeEach
    void setUp() {
        service = new LeaveService(repository, employeeRepository, idGenerator, notificationService);
        lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();
    }

    private LeaveRequest leave(LocalDate start, LocalDate end) {
        LeaveRequest l = new LeaveRequest();
        l.setEmployeeId("EMP-0002");
        l.setStartDate(start);
        l.setEndDate(end);
        l.setReason("Family holiday");
        return l;
    }

    @Test
    void requestingLeaveMakesItPendingAndNotifiesAdmins() {
        when(employeeRepository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        when(idGenerator.nextId(eq(IdGeneratorService.LEAVE), any())).thenReturn("LVE-0001");
        when(repository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest saved = service.request(leave(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5)));

        assertEquals("LVE-0001", saved.getLeaveId());
        assertEquals(LeaveRequest.Status.PENDING, saved.getStatus());
        assertEquals(LeaveRequest.LeaveType.ANNUAL, saved.getLeaveType());
        assertEquals("Lerato Dlamini", saved.getEmployeeName());
        verify(notificationService).notify(eq(Notification.Type.LEAVE_REQUEST), anyString(), anyString(), eq("LVE-0001"), eq(Notification.AUDIENCE_ADMIN));
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        when(employeeRepository.findById("EMP-0002")).thenReturn(Optional.of(lerato));
        assertThrows(IllegalArgumentException.class, () -> service.request(leave(LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 1))));
    }

    @Test
    void approvingNotifiesTheEmployeeAndCannotBeDoneTwice() {
        LeaveRequest pending = leave(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2));
        pending.setLeaveId("LVE-0001");
        pending.setLeaveType(LeaveRequest.LeaveType.SICK);
        pending.setStatus(LeaveRequest.Status.PENDING);
        Employee admin = new Employee.Builder().setEmpId("EMP-0001").setFName("System").setLName("Admin").build();
        when(repository.findById("LVE-0001")).thenReturn(Optional.of(pending));
        when(employeeRepository.findById("EMP-0001")).thenReturn(Optional.of(admin));
        when(repository.save(any(LeaveRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        LeaveRequest approved = service.decide("LVE-0001", true, "EMP-0001", "Get well soon");

        assertEquals(LeaveRequest.Status.APPROVED, approved.getStatus());
        assertEquals("System Admin", approved.getReviewedBy());
        verify(notificationService).notify(eq(Notification.Type.LEAVE_DECISION), anyString(), anyString(), eq("LVE-0001"), eq("EMP-0002"));
        assertThrows(IllegalStateException.class, () -> service.decide("LVE-0001", false, "EMP-0001", null));
    }
}
