package za.ac.cput.adp_assignment02.factory;

/*
LeaveRequestFactory.java
LeaveRequest Factory class
Author: Robyn Dominique Stevens (222201789)
Date: 11 November 2026
*/


import za.ac.cput.adp_assignment02.domain.LeaveRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveRequestFactory {
    /* A new request is always PENDING until an admin approves or declines it */
    public static LeaveRequest createLeaveRequest(String leaveId, String employeeId, String employeeName, LeaveRequest.LeaveType leaveType,
                                                  LocalDate startDate, LocalDate endDate, String reason) {
        if (employeeId == null || employeeId.isBlank() || startDate == null || endDate == null || endDate.isBefore(startDate)) {
            return null;
        }
        LeaveRequest leave = new LeaveRequest();
        leave.setLeaveId(leaveId);
        leave.setEmployeeId(employeeId);
        leave.setEmployeeName(employeeName);
        leave.setLeaveType(leaveType == null ? LeaveRequest.LeaveType.ANNUAL : leaveType);
        leave.setStartDate(startDate);
        leave.setEndDate(endDate);
        leave.setReason(reason);
        leave.setStatus(LeaveRequest.Status.PENDING);
        leave.setRequestedAt(LocalDateTime.now());
        return leave;
    }

    public static LeaveRequest createSickLeave(String leaveId, String employeeId, String employeeName, LocalDate startDate, LocalDate endDate) {
        return createLeaveRequest(leaveId, employeeId, employeeName, LeaveRequest.LeaveType.SICK, startDate, endDate, "Sick leave");
    }
}
