package za.ac.cput.adp_assignment02.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.ac.cput.adp_assignment02.domain.Attendance;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.repository.AttendanceRepository;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

/*
 * AttendanceService.java
 * Clock in / clock out for employees. Admins are notified when someone clocks in or out.
 */
@Service
public class AttendanceService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final AttendanceRepository repository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public AttendanceService(AttendanceRepository repository, EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Attendance clockIn(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        if (repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId).isPresent()) {
            throw new IllegalStateException("You are already clocked in.");
        }
        Attendance shift = repository.save(new Attendance(employeeId, employee.getFullName(), LocalDateTime.now()));
        notificationService.notify(Notification.Type.CLOCK_IN, employee.getFullName() + " clocked in",
                employee.getFullName() + " clocked in at " + shift.getClockIn().format(TIME) + ".",
                employeeId, Notification.AUDIENCE_ADMIN);
        return shift;
    }

    @Transactional
    public Attendance clockOut(String employeeId) {
        Attendance shift = repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId)
                .orElseThrow(() -> new IllegalStateException("You are not clocked in."));
        shift.setClockOut(LocalDateTime.now());
        Attendance saved = repository.save(shift);
        long minutes = saved.getMinutesWorked();
        notificationService.notify(Notification.Type.CLOCK_OUT, saved.getEmployeeName() + " clocked out",
                String.format("%s clocked out at %s after %dh %02dm.", saved.getEmployeeName(), saved.getClockOut().format(TIME), minutes / 60, minutes % 60),
                employeeId, Notification.AUDIENCE_ADMIN);
        return saved;
    }

    /* The open shift for an employee, or null if they are not clocked in */
    public Attendance currentShift(String employeeId) {
        return repository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId).orElse(null);
    }

    public List<Attendance> getAll() {
        return repository.findAllByOrderByClockInDesc();
    }

    public List<Attendance> getForEmployee(String employeeId) {
        return repository.findByEmployeeIdOrderByClockInDesc(employeeId);
    }
}
