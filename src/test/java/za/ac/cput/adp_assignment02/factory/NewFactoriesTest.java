package za.ac.cput.adp_assignment02.factory;

/*
 * NewFactoriesTest.java
 * Tests for the Attendance, LeaveRequest, Notification, StockAlert, Payment and ProductCategory factories.
 * Author: David Daniel Sepkitt (240046935)
 * Date: 11 November 2026
 */

import org.junit.jupiter.api.Test;
import za.ac.cput.adp_assignment02.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NewFactoriesTest {

    private final Employee lerato = new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").build();
    private final Product flour = new Product.Builder().setId("PRD-0010").setName("Cake Flour 10kg").setPrice(149.99).setQuantity(4).setReorderLevel(10).build();

    @Test
    void attendanceClockInStartsAnOpenShift() {
        Attendance a = AttendanceFactory.clockIn("EMP-0002", "Lerato Dlamini");
        assertNotNull(a);
        assertEquals("EMP-0002", a.getEmployeeId());
        assertNotNull(a.getClockIn());
        assertNull(a.getClockOut());
        assertNull(AttendanceFactory.clockIn(" ", "Nobody"));
    }

    @Test
    void attendanceShiftCannotEndBeforeItStarts() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 11, 8, 0);
        assertNotNull(AttendanceFactory.createShift("EMP-0002", "Lerato Dlamini", start, start.plusHours(8)));
        assertNull(AttendanceFactory.createShift("EMP-0002", "Lerato Dlamini", start, start.minusHours(1)));
    }

    @Test
    void leaveRequestStartsPendingAndChecksTheDates() {
        LeaveRequest l = LeaveRequestFactory.createLeaveRequest("LVE-0001", "EMP-0002", "Lerato Dlamini", LeaveRequest.LeaveType.ANNUAL,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 5), "Family holiday");
        assertNotNull(l);
        assertEquals(LeaveRequest.Status.PENDING, l.getStatus());
        assertEquals(5, l.getDays());
        assertEquals(LeaveRequest.LeaveType.SICK, LeaveRequestFactory.createSickLeave("LVE-0002", "EMP-0002", "Lerato", LocalDate.now(), LocalDate.now()).getLeaveType());
        assertNull(LeaveRequestFactory.createLeaveRequest("LVE-0003", "EMP-0002", "Lerato", null, LocalDate.of(2026, 12, 5), LocalDate.of(2026, 12, 1), null));
    }

    @Test
    void notificationsGoToTheRightAudience() {
        Notification n = NotificationFactory.forAdmins(Notification.Type.LOW_STOCK, "Low stock: Cake Flour", "Only 4 left.", "PRD-0010");
        assertEquals(Notification.AUDIENCE_ADMIN, n.getAudience());
        assertFalse(n.isSeen());
        assertEquals("EMP-0002", NotificationFactory.forEmployee("EMP-0002", Notification.Type.LEAVE_DECISION, "Leave approved", "Enjoy!", "LVE-0001").getAudience());
        assertNull(NotificationFactory.createNotification(null, "x", "y", null, null));
    }

    @Test
    void stockAlertStartsOpenWithATidyNote() {
        StockAlert alert = StockAlertFactory.createStockAlert(flour, lerato, "  Shelf is empty  ");
        assertNotNull(alert);
        assertEquals(StockAlert.Status.OPEN, alert.getStatus());
        assertEquals("Shelf is empty", alert.getNote());
        assertEquals(4, alert.getQuantityAtReport());
        assertNull(StockAlertFactory.createStockAlert(null, lerato, null));
    }

    @Test
    void paymentIsRoundedAndNeedsAPositiveAmount() {
        Order order = new Order.Builder().setOrderId("ORD-0010").setCustomerId("CUS-0001").setCustomerName("Sipho Ndlovu").build();
        Payment p = PaymentFactory.createPayment(order, 150.456, "eft", "Lerato Dlamini", null);
        assertNotNull(p);
        assertEquals(150.46, p.getAmount(), 0.001);
        assertEquals("EFT", p.getMethod());
        assertEquals("CUS-0001", p.getCustomerId());
        assertNull(PaymentFactory.createPayment(order, 0, "CASH", null, null));
    }

    @Test
    void categoryNameIsTidiedAndColourChecked() {
        ProductCategory c = ProductCategoryFactory.createCategory("  Cleaning   Supplies ", "spray", "#2563B8");
        assertEquals("Cleaning Supplies", c.getName());
        assertEquals("#2563b8", c.getColor());
        assertNull(ProductCategoryFactory.createCategory("Electronics", null, "blue").getColor());
        assertNull(ProductCategoryFactory.createCategory(" "));
    }
}
