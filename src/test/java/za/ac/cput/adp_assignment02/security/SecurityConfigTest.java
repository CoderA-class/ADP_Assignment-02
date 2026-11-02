package za.ac.cput.adp_assignment02.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * SecurityConfigTest.java
 * Blackboard notes 9.8 exercise: "users with different roles and authorities on which methods
 * to call and display appropriate messages if errors occur".
 * Uses the in-memory H2 test database (src/test/resources/application.properties).
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void healthCheckIsPublic() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    void notLoggedInGets401WithAMessage() throws Exception {
        mvc.perform(get("/products/all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Please sign in to continue."));
    }

    @Test
    void theDefaultAdminCanLogInWithTheFormAndWrongPasswordsAreRejected() throws Exception {
        // the DataSeeder creates admin@wholesale.local / Admin@123 on start-up
        mvc.perform(post("/auth/login").with(csrf()).param("email", "admin@wholesale.local").param("password", "Admin@123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessLevel").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(post("/auth/login").with(csrf()).param("email", "admin@wholesale.local").param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Incorrect email or password."));
    }

    @Test
    void postsWithoutACsrfTokenAreRejected() throws Exception {
        mvc.perform(post("/auth/login").param("email", "admin@wholesale.local").param("password", "Admin@123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff@wholesale.local", roles = "EMPLOYEE")
    void employeesCanViewStockButNotManageStaffOrProducts() throws Exception {
        mvc.perform(get("/products/all")).andExpect(status().isOk());
        mvc.perform(get("/employees/all")).andExpect(status().isForbidden());
        mvc.perform(delete("/products/delete/PRD-0001").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(get("/leave/all")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@wholesale.local", roles = "ADMIN")
    void adminsCanManageStaff() throws Exception {
        mvc.perform(get("/employees/all")).andExpect(status().isOk());
        mvc.perform(get("/leave/all")).andExpect(status().isOk());
        mvc.perform(get("/attendance/all")).andExpect(status().isOk());
    }
}
