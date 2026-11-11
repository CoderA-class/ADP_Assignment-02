package za.ac.cput.adp_assignment02.factory;

/*
AttendanceFactory.java
Attendance Factory class
Author: Robyn Dominique Stevens (222201789)
Date: 11 November 2026
*/


import za.ac.cput.adp_assignment02.domain.Attendance;

import java.time.LocalDateTime;

public class AttendanceFactory {
    /* Starts a shift: clocked in now, not clocked out yet */
    public static Attendance clockIn(String employeeId, String employeeName) {
        if (employeeId == null || employeeId.isBlank()) {
            return null;
        }
        return new Attendance(employeeId, employeeName, LocalDateTime.now());
    }

    /* A complete shift, e.g. for history or tests */
    public static Attendance createShift(String employeeId, String employeeName, LocalDateTime clockIn, LocalDateTime clockOut) {
        if (employeeId == null || employeeId.isBlank() || clockIn == null || (clockOut != null && clockOut.isBefore(clockIn))) {
            return null;
        }
        Attendance shift = new Attendance(employeeId, employeeName, clockIn);
        shift.setClockOut(clockOut);
        return shift;
    }
}
