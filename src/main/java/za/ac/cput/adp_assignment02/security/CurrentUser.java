package za.ac.cput.adp_assignment02.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

import java.util.Optional;

/*
 * CurrentUser.java
 * "Obtaining information about the current user" (Blackboard notes 9.7.7):
 * reads the logged-in user from the SecurityContextHolder and looks up the matching employee.
 * Used by controllers so employees can only see/change their OWN leave, shifts, etc.
 */
@Component
public class CurrentUser {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final EmployeeRepository employeeRepository;

    public CurrentUser(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    private Authentication authentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    public Optional<Employee> employee() {
        Authentication auth = authentication();
        return auth == null ? Optional.empty() : employeeRepository.findByEmailIgnoreCase(auth.getName());
    }

    /* The logged-in employee, or a 403 if there isn't one */
    public Employee require() {
        return employee().orElseThrow(() -> new AccessDeniedException("Please log in again."));
    }

    public boolean isAdmin() {
        Authentication auth = authentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (ROLE_ADMIN.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /* Admins may act on anyone; employees only on themselves */
    public String requireSelfOrAdmin(String employeeId) {
        if (isAdmin()) {
            return employeeId;
        }
        String me = require().getEmpID();
        if (employeeId != null && !employeeId.isBlank() && !employeeId.equals(me)) {
            throw new AccessDeniedException("You can only view or change your own records.");
        }
        return me;
    }
}
