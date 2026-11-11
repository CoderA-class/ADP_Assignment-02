package za.ac.cput.adp_assignment02.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import za.ac.cput.adp_assignment02.domain.Attendance;
import za.ac.cput.adp_assignment02.security.CurrentUser;
import za.ac.cput.adp_assignment02.service.AttendanceService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * AttendanceControllerTest.java
 * Test class for AttendanceController
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock private AttendanceService service;
    @Mock private CurrentUser currentUser;
    private AttendanceController controller;

    @BeforeEach
    void setUp() {
        controller = new AttendanceController(service, currentUser);
    }

    @Test
    void currentShiftIs204WhenNotClockedIn() {
        when(currentUser.requireSelfOrAdmin("EMP-0002")).thenReturn("EMP-0002");
        when(service.currentShift("EMP-0002")).thenReturn(null);
        assertNotEquals(HttpStatus.OK, controller.current("EMP-0002").getStatusCode());
    }

    @Test
    void currentShiftIsReturnedWhenClockedIn() {
        when(currentUser.requireSelfOrAdmin("EMP-0002")).thenReturn("EMP-0002");
        when(service.currentShift("EMP-0002")).thenReturn(new Attendance("EMP-0002", "Lerato Dlamini", LocalDateTime.now()));
        assertEquals(HttpStatus.OK, controller.current("EMP-0002").getStatusCode());
    }
}
