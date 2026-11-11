package za.ac.cput.adp_assignment02.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import za.ac.cput.adp_assignment02.domain.Employee;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

import java.io.IOException;
import java.util.Optional;

/*
 * ActiveAccountFilter.java
 * If an admin bans (suspends) an employee who is still signed in, their very next request
 * ends the session and answers 401, so the app sends them back to the login screen.
 */
public final class ActiveAccountFilter extends OncePerRequestFilter {

    private final EmployeeRepository employeeRepository;

    public ActiveAccountFilter(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            Optional<Employee> employee = employeeRepository.findByEmailIgnoreCase(auth.getName());
            if (employee.isPresent() && employee.get().getEmpStatus() != Employee.EmpStatus.ACTIVE) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
                SecurityContextHolder.clearContext();
                String path = request.getContextPath().isEmpty() ? "/" : request.getContextPath();
                response.addHeader("Set-Cookie", "remember-me=; Max-Age=0; Path=" + path + "; HttpOnly");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Your account has been suspended. Please speak to an admin.\",\"code\":\"SUSPENDED\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
