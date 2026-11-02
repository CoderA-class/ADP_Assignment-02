package za.ac.cput.adp_assignment02.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import za.ac.cput.adp_assignment02.domain.Customer;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.domain.Product;
import za.ac.cput.adp_assignment02.domain.Supplier;
import za.ac.cput.adp_assignment02.repository.CustomerRepository;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;
import za.ac.cput.adp_assignment02.repository.ProductRepository;
import za.ac.cput.adp_assignment02.repository.SupplierRepository;
import za.ac.cput.adp_assignment02.service.CustomerService;
import za.ac.cput.adp_assignment02.service.EmployeeService;
import za.ac.cput.adp_assignment02.service.ProductService;
import za.ac.cput.adp_assignment02.service.SupplierService;

import java.time.LocalDate;

/*
 * DataSeeder.java
 * Runs once when the app starts:
 *  1. Makes sure there is always an admin account to log in with.
 *  2. Gives older employees (created before logins existed) a default password.
 *  3. If the database is completely empty, adds some sample data so the app isn't blank.
 *     Turn this off with app.seed-demo-data=false in application.properties.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeService employeeService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final boolean seedDemoData;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(EmployeeRepository employeeRepository, CustomerRepository customerRepository,
                      ProductRepository productRepository, SupplierRepository supplierRepository,
                      EmployeeService employeeService, CustomerService customerService,
                      ProductService productService, SupplierService supplierService,
                      @Value("${app.seed-demo-data:true}") boolean seedDemoData,
                      @Value("${app.default-admin.email:admin@wholesale.local}") String adminEmail,
                      @Value("${app.default-admin.password:Admin@123}") String adminPassword) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
        this.employeeService = employeeService;
        this.customerService = customerService;
        this.productService = productService;
        this.supplierService = supplierService;
        this.seedDemoData = seedDemoData;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        // Older employees have no password/access level yet
        for (Employee e : employeeRepository.findAll()) {
            boolean changed = false;
            if (e.getPassword() == null || e.getPassword().isBlank()) {
                e.setPassword(employeeService.hash(EmployeeService.DEFAULT_PASSWORD));
                changed = true;
            }
            if (e.getEmpStatus() == null) {
                e.setEmpStatus(Employee.EmpStatus.ACTIVE);
                changed = true;
            }
            if (changed) {
                employeeRepository.save(e);
                log.info("Employee {} ({}) can now log in with the default password.", e.getFullName(), e.getEmail());
            }
        }

        if (employeeRepository.countByAccessLevel(Employee.AccessLevel.ADMIN) == 0) {
            Employee admin = new Employee.Builder()
                    .setFName("System").setLName("Admin")
                    .setEmail(adminEmail).setPhone("")
                    .setRole("Administrator").setSalary(0)
                    .setAccessLevel(Employee.AccessLevel.ADMIN)
                    .setPassword(adminPassword)
                    .build();
            employeeService.create(admin);
            log.info("==================================================================");
            log.info(" Created the default admin account");
            log.info("   Email:    {}", adminEmail);
            log.info("   Password: {}   (change it under My Profile after logging in)", adminPassword);
            log.info("==================================================================");
        }

        if (seedDemoData && productRepository.count() == 0 && customerRepository.count() == 0 && supplierRepository.count() == 0) {
            seedDemo();
        }
    }

    private void seedDemo() {
        log.info("Database is empty - adding sample suppliers, products, customers and an employee.");
        Supplier fresh = supplierService.create(new Supplier.SupplierBuilder().setName("Cape Fresh Distributors")
                .setContactPerson("Thandi Mokoena").setEmail("orders@capefresh.co.za").setPhone("021 555 0101")
                .setAddress("12 Harbour Rd, Cape Town").build());
        Supplier bev = supplierService.create(new Supplier.SupplierBuilder().setName("Sunrise Beverages")
                .setContactPerson("Pieter Botha").setEmail("sales@sunrisebev.co.za").setPhone("021 555 0202")
                .setAddress("8 Industria Way, Epping").build());
        Supplier pantry = supplierService.create(new Supplier.SupplierBuilder().setName("Karoo Pantry Wholesale")
                .setContactPerson("Ayesha Khan").setEmail("hello@karoopantry.co.za").setPhone("023 555 0303")
                .setAddress("3 Main St, Worcester").build());

        product("White Bread 700g (crate of 12)", 156.00, 40, "Bakery", fresh, 10);
        product("Brown Bread 700g (crate of 12)", 162.00, 8, "Bakery", fresh, 10);
        product("Full Cream Milk 1L (6 pack)", 99.90, 60, "Dairy", fresh, 15);
        product("Cheddar Cheese 900g", 129.99, 5, "Dairy", fresh, 8);
        product("Cola 2L (6 pack)", 119.00, 75, "Beverages", bev, 20);
        product("Still Water 500ml (24 pack)", 89.00, 120, "Beverages", bev, 25);
        product("Orange Juice 1L (6 pack)", 134.50, 0, "Beverages", bev, 10);
        product("Long Grain Rice 10kg", 189.99, 30, "Pantry", pantry, 10);
        product("White Sugar 10kg", 179.00, 25, "Pantry", pantry, 10);
        product("Cake Flour 10kg", 149.99, 9, "Pantry", pantry, 10);
        product("Sunflower Oil 5L", 214.00, 18, "Pantry", pantry, 6);
        product("Maize Meal 10kg", 109.00, 50, "Pantry", pantry, 12);

        customer("Sipho", "Ndlovu", "Ndlovu Spaza Shop", "sipho@ndlovuspaza.co.za", "072 111 2233", "45 Long St, Khayelitsha", LocalDate.now().minusDays(3));
        customer("Megan", "Adams", "Adams Corner Café", "megan@adamscafe.co.za", "082 444 5566", "7 Kloof St, Gardens", LocalDate.now().minusDays(12));
        customer("Johan", "van Wyk", "Van Wyk Groceries", "johan@vwgroceries.co.za", "083 777 8899", "19 Voortrekker Rd, Bellville", LocalDate.now().minusDays(45));
        customer("Fatima", "Patel", "Patel Mini Market", "fatima@patelmm.co.za", "071 222 3344", "2 Station Rd, Athlone", null);

        Employee staff = new Employee.Builder().setFName("Lerato").setLName("Dlamini")
                .setEmail("lerato@wholesale.local").setPhone("074 123 4567").setRole("Sales Assistant")
                .setSalary(14500).setAccessLevel(Employee.AccessLevel.EMPLOYEE).setPassword("Staff@123").build();
        employeeService.create(staff);
        log.info("Sample employee login: lerato@wholesale.local / Staff@123");
    }

    private void product(String name, double price, int qty, String category, Supplier supplier, int reorder) {
        productService.create(new Product.Builder().setName(name).setPrice(price).setQuantity(qty)
                .setCategory(category).setSupplierId(supplier.getSupplierId()).setReorderLevel(reorder).build());
    }

    private void customer(String first, String last, String company, String email, String phone, String address, LocalDate lastPurchase) {
        Customer c = customerService.create(new Customer.Builder().setFirstName(first).setLastName(last).setCompany(company)
                .setEmail(email).setPhoneNumber(phone).setAddress(address)
                .setCreatedDate(LocalDate.now().minusDays(60)).build());
        if (lastPurchase != null) {
            c.setLastPurchaseDate(lastPurchase);
            customerRepository.save(c);
        }
    }
}
