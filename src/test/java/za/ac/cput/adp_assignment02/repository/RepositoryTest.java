package za.ac.cput.adp_assignment02.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Notification;
import za.ac.cput.adp_assignment02.domain.OrderItem;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.Supplier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
 * RepositoryTest.java
 * Checks the custom search queries against an in-memory H2 database
 * (see src/test/resources/application.properties), so MySQL is not needed for tests.
 */
@DataJpaTest
class RepositoryTest {

    @Autowired private CustomerRepository customerRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private NotificationRepository notificationRepository;

    @Test
    void customerSearchMatchesFirstLastFullNameAndShop() {
        customerRepository.save(new Customer.Builder().setCustID("CUS-0001").setFirstName("Johan").setLastName("van Wyk").setCompany("Van Wyk Groceries").build());
        customerRepository.save(new Customer.Builder().setCustID("CUS-0002").setFirstName("Megan").setLastName("Adams").setCompany("Adams Café").build());

        assertEquals(1, customerRepository.searchByName("joh").size());
        assertEquals(1, customerRepository.searchByName("johan van").size());
        assertEquals(1, customerRepository.searchByName("CAFÉ").size());
        assertEquals(0, customerRepository.searchByName("zzz").size());
    }

    @Test
    void employeeLookupsWork() {
        employeeRepository.save(new Employee.Builder().setEmpId("EMP-0001").setFName("System").setLName("Admin").setEmail("Admin@Wholesale.local")
                .setSalary(0).setEmpStatus(Employee.EmpStatus.ACTIVE).setAccessLevel(Employee.AccessLevel.ADMIN).build());
        employeeRepository.save(new Employee.Builder().setEmpId("EMP-0002").setFName("Lerato").setLName("Dlamini").setEmail("lerato@wholesale.local")
                .setSalary(14500).setEmpStatus(Employee.EmpStatus.ACTIVE).setAccessLevel(Employee.AccessLevel.EMPLOYEE).build());

        assertTrue(employeeRepository.findByEmailIgnoreCase("admin@wholesale.local").isPresent());
        assertEquals(1, employeeRepository.countByAccessLevel(Employee.AccessLevel.ADMIN));
        List<Employee> found = employeeRepository.searchByName("lera");
        assertEquals(1, found.size());
        assertEquals("EMP-0002", found.get(0).getEmpID());
    }

    @Test
    void productSearchAndSupplierLink() {
        supplierRepository.save(new Supplier.SupplierBuilder().setSupplierId("SUP-0001").setName("Karoo Pantry Wholesale").build());
        productRepository.save(new Product.Builder().setId("PRD-0001").setName("White Sugar 10kg").setPrice(179).setQuantity(25).setCategory("Pantry").setSupplierId("SUP-0001").build());
        productRepository.save(new Product.Builder().setId("PRD-0002").setName("Cola 2L").setPrice(119).setQuantity(75).setCategory("Beverages").build());

        assertEquals(1, productRepository.searchByName("sugar").size());
        assertEquals(1, productRepository.searchByName("bever").size());   // matches the category too
        assertEquals(1, productRepository.findBySupplierId("SUP-0001").size());
        assertEquals(1, supplierRepository.searchByName("karoo").size());
    }

    @Test
    void orderItemsCanBeFoundByOrder() {
        orderItemRepository.save(new OrderItem.OrderItemBuilder().setOrderItemId("ITM-0001").setOrderId("ORD-0001").setProductId("PRD-0001").setQuantity(2).setLineTotal(30).build());
        orderItemRepository.save(new OrderItem.OrderItemBuilder().setOrderItemId("ITM-0002").setOrderId("ORD-0002").setProductId("PRD-0001").setQuantity(1).setLineTotal(15).build());
        assertEquals(1, orderItemRepository.findByOrderId("ORD-0001").size());
    }

    @Test
    void notificationsAreFilteredByAudienceAndDeduplicated() {
        notificationRepository.save(new Notification(Notification.Type.LOW_STOCK, "Low stock", "Only 3 left", "PRD-0001", Notification.AUDIENCE_ALL));
        notificationRepository.save(new Notification(Notification.Type.LEAVE_REQUEST, "Leave", "5 days", "LVE-0001", Notification.AUDIENCE_ADMIN));
        notificationRepository.save(new Notification(Notification.Type.LEAVE_DECISION, "Approved", "Enjoy", "LVE-0001", "EMP-0002"));

        assertEquals(2, notificationRepository.findForAudiences(List.of("ALL", "EMP-0002")).size());
        assertEquals(2, notificationRepository.findForAudiences(List.of("ALL", "ADMIN")).size());
        assertTrue(notificationRepository.existsByTypeAndRefIdAndSeenFalse(Notification.Type.LOW_STOCK, "PRD-0001"));
        assertFalse(notificationRepository.existsByTypeAndRefIdAndSeenFalse(Notification.Type.LOW_STOCK, "PRD-0002"));
    }
}
