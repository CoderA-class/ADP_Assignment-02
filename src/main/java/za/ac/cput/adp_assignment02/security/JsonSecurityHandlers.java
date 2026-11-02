package za.ac.cput.adp_assignment02.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import za.ac.cput.adp_assignment02.repository.EmployeeRepository;

import java.io.IOException;
import java.util.Map;

/*
 * JsonSecurityHandlers.java
 * The React app talks JSON, so instead of redirecting to login.jsp / accessDenied.jsp
 * (as in the Blackboard notes) these handlers send short JSON messages that the
 * frontend shows to the user ("display appropriate messages if errors occur").
 */
@Component
public class JsonSecurityHandlers implements AuthenticationSuccessHandler, AuthenticationFailureHandler,
        AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final EmployeeRepository employeeRepository;

    public JsonSecurityHandlers(ObjectMapper objectMapper, EmployeeRepository employeeRepository) {
        this.objectMapper = objectMapper;
        this.employeeRepository = employeeRepository;
    }

    /* Login worked: send back the employee (the password is never included) */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        Object body = employeeRepository.findByEmailIgnoreCase(authentication.getName())
                .<Object>map(e -> e)
                .orElse(Map.of("email", authentication.getName()));
        write(response, HttpStatus.OK, body);
    }

    /* Login failed */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String message;
        if (exception instanceof DisabledException) {
            message = "This account is suspended or no longer active. Please speak to an admin.";
        } else if (exception instanceof LockedException) {
            message = "This account is locked. Please speak to an admin.";
        } else if (exception instanceof BadCredentialsException) {
            message = "Incorrect email or password.";
        } else {
            message = "We couldn't sign you in. Please try again.";
        }
        write(response, HttpStatus.UNAUTHORIZED, Map.of("message", message));
    }

    /* Not logged in (or the session expired) */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, Map.of("message", "Please sign in to continue.", "code", "UNAUTHENTICATED"));
    }

    /* Logged in, but the role isn't allowed to do this (e.g. an employee trying to add staff) */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        if (exception instanceof CsrfException) {
            write(response, HttpStatus.FORBIDDEN, Map.of("message", "Your security token expired. Please try again.", "code", "CSRF"));
            return;
        }
        write(response, HttpStatus.FORBIDDEN, Map.of("message", "You don't have permission to do that. Ask an admin if you need access.", "code", "FORBIDDEN"));
    }

    private void write(HttpServletResponse response, HttpStatus status, Object body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), body);
    }
}
