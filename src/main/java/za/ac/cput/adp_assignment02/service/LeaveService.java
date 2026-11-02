package za.ac.cput.adp_assignment02.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.LeaveRequest;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;
import za.ac.cput.adp_assignment02.repository.LeaveRequestRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

/*
 * LeaveService.java
 * Employees request leave; admins approve or decline it. Both sides get notified.
 */
@Service
public class LeaveService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final LeaveRequestRepository repository;
    private final EmployeeRepository employeeRepository;
    private final IdGeneratorService idGenerator;
    private final NotificationService notificationService;

    public LeaveService(LeaveRequestRepository repository, EmployeeRepository employeeRepository,
                        IdGeneratorService idGenerator, NotificationService notificationService) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.idGenerator = idGenerator;
        this.notificationService = notificationService;
    }

    @Transactional
    public LeaveRequest request(LeaveRequest leave) {
        if (leave == null || Helper.isBlank(leave.getEmployeeId())) {
            throw new IllegalArgumentException("Employee is required.");
        }
        Employee employee = employeeRepository.findById(leave.getEmployeeId())
                .orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        if (leave.getStartDate() == null || leave.getEndDate() == null) {
            throw new IllegalArgumentException("Please choose a start and end date.");
        }
        if (leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new IllegalArgumentException("The end date can't be before the start date.");
        }
        if (leave.getLeaveType() == null) {
            leave.setLeaveType(LeaveRequest.LeaveType.ANNUAL);
        }
        leave.setLeaveId(idGenerator.nextId(IdGeneratorService.LEAVE, repository::existsById));
        leave.setEmployeeName(employee.getFullName());
        leave.setStatus(LeaveRequest.Status.PENDING);
        leave.setRequestedAt(LocalDateTime.now());
        leave.setReviewedBy(null);
        leave.setReviewedAt(null);
        LeaveRequest saved = repository.save(leave);

        notificationService.notify(Notification.Type.LEAVE_REQUEST, "Leave request from " + employee.getFullName(),
                String.format("%s leave, %s to %s (%d day%s).", pretty(saved.getLeaveType().name()),
                        saved.getStartDate().format(DATE), saved.getEndDate().format(DATE), saved.getDays(), saved.getDays() == 1 ? "" : "s"),
                saved.getLeaveId(), Notification.AUDIENCE_ADMIN);
        return saved;
    }

    @Transactional
    public LeaveRequest decide(String leaveId, boolean approve, String reviewerId, String comment) {
        LeaveRequest leave = repository.findById(leaveId).orElseThrow(() -> new NoSuchElementException("Leave request was not found."));
        if (leave.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalStateException("This request has already been " + leave.getStatus().name().toLowerCase() + ".");
        }
        String reviewerName = Helper.isBlank(reviewerId) ? "Admin"
                : employeeRepository.findById(reviewerId).map(Employee::getFullName).orElse("Admin");
        leave.setStatus(approve ? LeaveRequest.Status.APPROVED : LeaveRequest.Status.DECLINED);
        leave.setReviewedBy(reviewerName);
        leave.setReviewedAt(LocalDateTime.now());
        leave.setReviewComment(Helper.isBlank(comment) ? null : comment.trim());
        LeaveRequest saved = repository.save(leave);

        notificationService.notify(Notification.Type.LEAVE_DECISION,
                "Leave " + (approve ? "approved" : "declined"),
                String.format("Your %s leave (%s to %s) was %s by %s.%s", pretty(saved.getLeaveType().name()).toLowerCase(),
                        saved.getStartDate().format(DATE), saved.getEndDate().format(DATE),
                        approve ? "approved" : "declined", reviewerName,
                        saved.getReviewComment() == null ? "" : " Note: " + saved.getReviewComment()),
                saved.getLeaveId(), saved.getEmployeeId());
        return saved;
    }

    @Transactional
    public LeaveRequest cancel(String leaveId, String employeeId) {
        LeaveRequest leave = repository.findById(leaveId).orElseThrow(() -> new NoSuchElementException("Leave request was not found."));
        if (!leave.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("You can only cancel your own leave requests.");
        }
        if (leave.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled.");
        }
        leave.setStatus(LeaveRequest.Status.CANCELLED);
        return repository.save(leave);
    }

    public List<LeaveRequest> getAll() {
        return repository.findAllByOrderByRequestedAtDesc();
    }

    public List<LeaveRequest> getForEmployee(String employeeId) {
        return repository.findByEmployeeIdOrderByRequestedAtDesc(employeeId);
    }

    private static String pretty(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
