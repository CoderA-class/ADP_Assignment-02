package za.ac.cput.adp_assignment02.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.Attendance;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.AttendanceService;

import java.util.List;

/*
 * AttendanceController.java
 * Clock in / clock out and the attendance register.
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final CurrentUser currentUser;

    public AttendanceController(AttendanceService attendanceService, CurrentUser currentUser) {
        this.attendanceService = attendanceService;
        this.currentUser = currentUser;
    }

    @PostMapping("/clock-in/{employeeId}")
    public Attendance clockIn(@PathVariable String employeeId) {
        return attendanceService.clockIn(currentUser.requireSelfOrAdmin(employeeId));
    }

    @PostMapping("/clock-out/{employeeId}")
    public Attendance clockOut(@PathVariable String employeeId) {
        return attendanceService.clockOut(currentUser.requireSelfOrAdmin(employeeId));
    }

    /* 200 with the open shift, or 204 (no content) if not clocked in */
    @GetMapping("/current/{employeeId}")
    public ResponseEntity<Attendance> current(@PathVariable String employeeId) {
        Attendance shift = attendanceService.currentShift(currentUser.requireSelfOrAdmin(employeeId));
        return shift == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(shift);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Attendance> getAll() {
        return attendanceService.getAll();
    }

    @GetMapping("/employee/{employeeId}")
    public List<Attendance> forEmployee(@PathVariable String employeeId) {
        return attendanceService.getForEmployee(currentUser.requireSelfOrAdmin(employeeId));
    }
}
