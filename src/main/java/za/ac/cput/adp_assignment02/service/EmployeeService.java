package za.ac.cput.adp_assignment02.service;

/*
EmployeeService.java
Employee Service Implementation
Author: Robyn Dominique Stevens (222201789)
Date: 10 August 2026
Updated: auto-generates the employee ID, hashes passwords, protects the last admin account.
*/

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;
import za.ac.cput.adp_assignment02.util.Helper;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EmployeeService implements IEmployeeService {

    /* Used when an admin adds an employee without typing a password */
    public static final String DEFAULT_PASSWORD = "Welcome@123";

    private final EmployeeRepository repository;
    private final IdGeneratorService idGenerator;
    private final PasswordEncoder passwordEncoder;   // BCrypt, defined in security/SecurityConfig

    public EmployeeService(EmployeeRepository repository, IdGeneratorService idGenerator, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Employee create(Employee employee) {
        validate(employee);
        repository.findByEmailIgnoreCase(employee.getEmail().trim()).ifPresent(e -> {
            throw new IllegalStateException("An employee with the email " + e.getEmail() + " already exists.");
        });
        if (Helper.isBlank(employee.getEmpID())) {
            employee.setEmpID(idGenerator.nextId(IdGeneratorService.EMPLOYEE, repository::existsById));
        } else if (repository.existsById(employee.getEmpID())) {
            throw new IllegalStateException("An employee with ID " + employee.getEmpID() + " already exists.");
        }
        String rawPassword = Helper.isBlank(employee.getPassword()) ? DEFAULT_PASSWORD : employee.getPassword();
        employee.setPassword(passwordEncoder.encode(rawPassword));
        if (employee.getEmpStatus() == null) {
            employee.setEmpStatus(Employee.EmpStatus.ACTIVE);
        }
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }
        employee.setAccessLevel(employee.getAccessLevel());
        normalise(employee);
        return repository.save(employee);
    }

    @Override
    public Employee read(String empID) {
        return repository.findById(empID).orElse(null);
    }

    @Override
    public Employee update(Employee employee) {
        if (Helper.isBlank(employee.getEmpID())) {
            throw new IllegalArgumentException("Employee ID is required to update an employee.");
        }
        Employee existing = repository.findById(employee.getEmpID())
                .orElseThrow(() -> new NoSuchElementException("Employee " + employee.getEmpID() + " was not found."));
        validate(employee);
        repository.findByEmailIgnoreCase(employee.getEmail().trim()).ifPresent(e -> {
            if (!e.getEmpID().equals(employee.getEmpID())) {
                throw new IllegalStateException("Another employee already uses the email " + e.getEmail() + ".");
            }
        });
        boolean wasActiveAdmin = existing.getAccessLevel() == Employee.AccessLevel.ADMIN && existing.getEmpStatus() == Employee.EmpStatus.ACTIVE;
        boolean staysActiveAdmin = employee.getAccessLevel() == Employee.AccessLevel.ADMIN
                && (employee.getEmpStatus() == null || employee.getEmpStatus() == Employee.EmpStatus.ACTIVE);
        if (wasActiveAdmin && !staysActiveAdmin && activeAdminCount() <= 1) {
            throw new IllegalStateException("This is the only active admin account, so it can't lose admin access.");
        }
        // Keep the current password unless a new one was typed in.
        // A password set by an admin is temporary: the employee picks their own at the next sign-in.
        if (Helper.isBlank(employee.getPassword())) {
            employee.setPassword(existing.getPassword());
            employee.setMustChangePassword(existing.isMustChangePassword());
        } else {
            if (employee.getPassword().length() < 6) {
                throw new IllegalArgumentException("The password must be at least 6 characters.");
            }
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
            employee.setMustChangePassword(true);
        }
        if (employee.getEmpStatus() == null) {
            employee.setEmpStatus(existing.getEmpStatus());
        }
        if (employee.getHireDate() == null) {
            employee.setHireDate(existing.getHireDate());
        }
        employee.setAccessLevel(employee.getAccessLevel());
        normalise(employee);
        return repository.save(employee);
    }

    @Override
    public boolean delete(String empID) {
        Employee existing = repository.findById(empID).orElse(null);
        if (existing == null) {
            return false;
        }
        if (existing.getAccessLevel() == Employee.AccessLevel.ADMIN && existing.getEmpStatus() == Employee.EmpStatus.ACTIVE && activeAdminCount() <= 1) {
            throw new IllegalStateException("You can't delete the only active admin account.");
        }
        repository.deleteById(empID);
        return true;
    }

    @Override
    public List<Employee> getAll() {
        return repository.findAll();
    }

    public List<Employee> searchByName(String name) {
        return Helper.isBlank(name) ? getAll() : repository.searchByName(name.trim());
    }

    /* ---------- account helpers (login itself is handled by Spring Security) ---------- */

    /* An employee updating their own profile: only phone and photo can be changed here */
    public Employee updateOwnProfile(String empID, String phone, String imageUrl) {
        Employee employee = repository.findById(empID).orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        employee.setPhone(Helper.clean(phone));
        employee.setImageUrl(imageUrl);
        return repository.save(employee);
    }

    public Employee authenticate(String email, String rawPassword) {
        if (Helper.isBlank(email) || Helper.isBlank(rawPassword)) {
            return null;
        }
        Employee employee = repository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (employee == null || employee.getPassword() == null || !passwordEncoder.matches(rawPassword, employee.getPassword())) {
            return null;
        }
        if (employee.getEmpStatus() != Employee.EmpStatus.ACTIVE) {
            throw new IllegalStateException("This account is " + employee.getEmpStatus().name().toLowerCase() + ". Please speak to an admin.");
        }
        return employee;
    }

    public void changePassword(String empID, String currentPassword, String newPassword) {
        Employee employee = repository.findById(empID).orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        if (employee.getPassword() == null || !passwordEncoder.matches(currentPassword == null ? "" : currentPassword, employee.getPassword())) {
            throw new IllegalArgumentException("Your current password is incorrect.");
        }
        if (Helper.isBlank(newPassword) || newPassword.length() < 6) {
            throw new IllegalArgumentException("The new password must be at least 6 characters.");
        }
        if (passwordEncoder.matches(newPassword, employee.getPassword())) {
            throw new IllegalArgumentException("Choose a password that is different from your current one.");
        }
        employee.setPassword(passwordEncoder.encode(newPassword));
        employee.setMustChangePassword(false);
        repository.save(employee);
    }

    public Employee findByEmail(String email) {
        return Helper.isBlank(email) ? null : repository.findByEmailIgnoreCase(email.trim()).orElse(null);
    }

    /* Ban: the employee can no longer sign in, and is signed out straight away (see ActiveAccountFilter) */
    public Employee ban(String empID, String byEmpID) {
        Employee employee = repository.findById(empID).orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        if (empID.equals(byEmpID)) {
            throw new IllegalStateException("You can't ban your own account.");
        }
        if (employee.getAccessLevel() == Employee.AccessLevel.ADMIN && employee.getEmpStatus() == Employee.EmpStatus.ACTIVE && activeAdminCount() <= 1) {
            throw new IllegalStateException("This is the only active admin account, so it can't be banned.");
        }
        employee.setEmpStatus(Employee.EmpStatus.SUSPENDED);
        return repository.save(employee);
    }

    public Employee unban(String empID) {
        Employee employee = repository.findById(empID).orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        employee.setEmpStatus(Employee.EmpStatus.ACTIVE);
        return repository.save(employee);
    }

    /* Gives the employee a one-time password (shown to the admin once); they must change it when they sign in */
    public String resetPassword(String empID) {
        Employee employee = repository.findById(empID).orElseThrow(() -> new NoSuchElementException("Employee was not found."));
        String temporary = temporaryPassword();
        employee.setPassword(passwordEncoder.encode(temporary));
        employee.setMustChangePassword(true);
        repository.save(employee);
        return temporary;
    }

    private static final String LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    static String temporaryPassword() {
        StringBuilder sb = new StringBuilder("Nexus-");
        for (int i = 0; i < 4; i++) sb.append(RANDOM.nextInt(10));
        for (int i = 0; i < 2; i++) sb.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        return sb.toString();
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    private long activeAdminCount() {
        return repository.findAll().stream()
                .filter(e -> e.getAccessLevel() == Employee.AccessLevel.ADMIN && e.getEmpStatus() == Employee.EmpStatus.ACTIVE)
                .count();
    }

    private void validate(Employee employee) {
        if (employee == null || Helper.isBlank(employee.getFName())) {
            throw new IllegalArgumentException("Employee name is required.");
        }
        if (Helper.isBlank(employee.getEmail())) {
            throw new IllegalArgumentException("An email address is required (employees use it to log in).");
        }
        if (employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative.");
        }
    }

    private void normalise(Employee e) {
        e.setFName(Helper.clean(e.getFName()));
        e.setLName(Helper.clean(e.getLName()));
        e.setEmail(e.getEmail().trim());
        e.setPhone(Helper.clean(e.getPhone()));
        e.setRole(Helper.isBlank(e.getRole()) ? "Staff" : e.getRole().trim());
    }
}
