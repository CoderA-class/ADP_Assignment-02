package za.ac.cput.adp_assignment02.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/*
 * LeaveRequestTest.java
 * Tests for the LeaveRequest domain class
 */
class LeaveRequestTest {

    @Test
    void daysIncludesTheStartAndEndDate() {
        LeaveRequest leave = new LeaveRequest();
        leave.setStartDate(LocalDate.of(2026, 12, 1));
        leave.setEndDate(LocalDate.of(2026, 12, 5));
        assertEquals(5, leave.getDays());
    }

    @Test
    void oneDayLeaveIsOneDay() {
        LeaveRequest leave = new LeaveRequest();
        leave.setStartDate(LocalDate.of(2026, 12, 1));
        leave.setEndDate(LocalDate.of(2026, 12, 1));
        assertEquals(1, leave.getDays());
    }

    @Test
    void daysIsZeroWithoutDates() {
        assertEquals(0, new LeaveRequest().getDays());
    }
}
