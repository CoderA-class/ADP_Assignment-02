package za.ac.cput.adp_assignment02.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.ac.cput.adp_assignment02.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/*
 * NewRepositoryTest.java
 * The attendance, leave, stock alert, payment and category repositories against the in-memory H2 database.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */
@DataJpaTest
class NewRepositoryTest {

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private LeaveRequestRepository leaveRepository;
    @Autowired private StockAlertRepository stockAlertRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ProductCategoryRepository categoryRepository;

    @Test
    void anOpenShiftCanBeFound() {
        attendanceRepository.save(new Attendance("EMP-0002", "Lerato Dlamini", LocalDateTime.now().minusHours(2)));
        assertTrue(attendanceRepository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc("EMP-0002").isPresent());
        assertTrue(attendanceRepository.findFirstByEmployeeIdAndClockOutIsNullOrderByClockInDesc("EMP-0009").isEmpty());
    }

    @Test
    void leaveIsListedPerEmployee() {
        LeaveRequest leave = new LeaveRequest();
        leave.setLeaveId("LVE-0001");
        leave.setEmployeeId("EMP-0002");
        leave.setLeaveType(LeaveRequest.LeaveType.ANNUAL);
        leave.setStartDate(LocalDate.of(2026, 12, 1));
        leave.setEndDate(LocalDate.of(2026, 12, 5));
        leave.setStatus(LeaveRequest.Status.PENDING);
        leave.setRequestedAt(LocalDateTime.now());
        leaveRepository.save(leave);
        assertEquals(1, leaveRepository.findByEmployeeIdOrderByRequestedAtDesc("EMP-0002").size());
    }

    @Test
    void activeStockAlertsAreFoundByProduct() {
        Product flour = new Product.Builder().setId("PRD-0010").setName("Cake Flour 10kg").setQuantity(4).setReorderLevel(10).build();
        Employee lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();
        stockAlertRepository.save(new StockAlert(flour, lerato, null));
        assertEquals(1, stockAlertRepository.findByProductIdAndStatusIn("PRD-0010", EnumSet.of(StockAlert.Status.OPEN)).size());
        assertEquals(0, stockAlertRepository.findByProductIdAndStatusIn("PRD-0010", EnumSet.of(StockAlert.Status.RESOLVED)).size());
    }

    @Test
    void paymentsAreFoundByOrderAndCustomer() {
        Order order = new Order.Builder().setOrderId("ORD-0010").setCustomerId("CUS-0001").setCustomerName("Sipho Ndlovu").build();
        paymentRepository.save(new Payment(order, 150.0, "CASH", "Lerato Dlamini", null));
        assertEquals(1, paymentRepository.findByOrderIdOrderByPaidAtDesc("ORD-0010").size());
        assertEquals(1, paymentRepository.findByCustomerIdOrderByPaidAtDesc("CUS-0001").size());
    }

    @Test
    void categoriesAreFoundByNameIgnoringCase() {
        categoryRepository.save(new ProductCategory("Electronics", "mobile", "#2563b8"));
        assertTrue(categoryRepository.findByNameIgnoreCase("electronics").isPresent());
    }
}
