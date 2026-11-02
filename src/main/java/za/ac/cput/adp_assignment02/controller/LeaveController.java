package za.ac.cput.adp_assignment02.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.adp_assignment02.domain.LeaveRequest;
import za.ac.cput.adp_assignment02.dto.LeaveDecisionRequest;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.LeaveService;

import java.util.List;

/*
 * LeaveController.java
 * Leave requests: employees request/cancel, admins approve/decline.
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {
    private final LeaveService leaveService;
    private final CurrentUser currentUser;

    public LeaveController(LeaveService leaveService, CurrentUser currentUser) {
        this.leaveService = leaveService;
        this.currentUser = currentUser;
    }

    /* { employeeId, leaveType, startDate: "2026-12-01", endDate: "2026-12-05", reason } */
    @PostMapping("/request")
    public LeaveRequest request(@RequestBody LeaveRequest leave) {
        // employees always request leave for themselves; admins may request for anyone
        leave.setEmployeeId(currentUser.requireSelfOrAdmin(leave.getEmployeeId()));
        return leaveService.request(leave);
    }

    @PutMapping("/{leaveId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveRequest approve(@PathVariable String leaveId, @RequestBody(required = false) LeaveDecisionRequest decision) {
        return leaveService.decide(leaveId, true, currentUser.require().getEmpID(), decision == null ? null : decision.comment());
    }

    @PutMapping("/{leaveId}/decline")
    @PreAuthorize("hasRole('ADMIN')")
    public LeaveRequest decline(@PathVariable String leaveId, @RequestBody(required = false) LeaveDecisionRequest decision) {
        return leaveService.decide(leaveId, false, currentUser.require().getEmpID(), decision == null ? null : decision.comment());
    }

    /* You can only cancel your own pending request */
    @PutMapping("/{leaveId}/cancel")
    public LeaveRequest cancel(@PathVariable String leaveId) {
        return leaveService.cancel(leaveId, currentUser.require().getEmpID());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<LeaveRequest> getAll() {
        return leaveService.getAll();
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> forEmployee(@PathVariable String employeeId) {
        return leaveService.getForEmployee(currentUser.requireSelfOrAdmin(employeeId));
    }
}
