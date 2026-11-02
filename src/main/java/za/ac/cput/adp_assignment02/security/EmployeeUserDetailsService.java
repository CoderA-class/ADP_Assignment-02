package za.ac.cput.adp_assignment02.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

/*
 * EmployeeUserDetailsService.java
 * Loads a user for Spring Security from the employee table.
 *
 * This is the Java-config version of the Blackboard notes' (Chapter 9.6)
 *   <jdbc-user-service users-by-username-query="SELECT username, passwd, enabled ..."
 *                      authorities-by-username-query="SELECT u.username, r.rolename ..."/>
 *
 *   username  -> employee email
 *   password  -> BCrypt hash stored on the employee
 *   enabled   -> employee status is ACTIVE
 *   role      -> ROLE_ADMIN or ROLE_EMPLOYEE (from the employee's access level)
 */
@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public EmployeeUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("No employee with email " + email));
        return User.withUsername(employee.getEmail())
                .password(employee.getPassword() == null ? "" : employee.getPassword())
                .roles(employee.getAccessLevel().name())          // ADMIN -> ROLE_ADMIN, EMPLOYEE -> ROLE_EMPLOYEE
                .disabled(employee.getEmpStatus() != Employee.EmpStatus.ACTIVE)
                .build();
    }
}
