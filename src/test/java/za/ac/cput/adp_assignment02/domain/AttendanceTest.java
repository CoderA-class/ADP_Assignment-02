package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/*
 * AttendanceTest.java
 * Tests for the Attendance (clock in / clock out) domain class
 */
class AttendanceTest {

    @Test
    void completedShiftCountsMinutesBetweenClockInAndOut() {
        Attendance shift = new Attendance("EMP-0001", "Lerato Dlamini", LocalDateTime.of(2026, 11, 2, 8, 0));
        shift.setClockOut(LocalDateTime.of(2026, 11, 2, 16, 30));
        assertEquals(510, shift.getMinutesWorked());
        assertFalse(shift.isActive());
    }

    @Test
    void openShiftIsActive() {
        Attendance shift = new Attendance("EMP-0001", "Lerato Dlamini", LocalDateTime.now().minusMinutes(5));
        assertTrue(shift.isActive());
        assertTrue(shift.getMinutesWorked() >= 4);
    }
}
