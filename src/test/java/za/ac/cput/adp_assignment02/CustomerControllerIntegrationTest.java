package za.ac.cput.adp_assignment02;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsCustomerFromJsonRequest() throws Exception {
        mockMvc.perform(post("/customers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "custID": "C-HTTP-1",
                                  "firstName": "Ava",
                                  "lastName": "Mokoena",
                                  "email": "ava.integration@example.com",
                                  "phoneNumber": "0821234567",
                                  "address": "Cape Town"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custID").value("C-HTTP-1"))
                .andExpect(jsonPath("$.firstName").value("Ava"));
    }

    @Test
    void createsEmployeesForEachSupportedStatus() throws Exception {
        String[] statuses = {"ACTIVE", "SUSPENDED", "TERMINATED"};

        for (int index = 0; index < statuses.length; index++) {
            String employeeId = "E-HTTP-" + index;
            mockMvc.perform(post("/employees/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "empID": "%s",
                                      "fName": "Mia",
                                      "lName": "Dlamini",
                                      "salary": 42000.50,
                                      "email": "mia.%s@example.com",
                                      "phone": "0821234567",
                                      "role": "Analyst",
                                      "empStatus": "%s"
                                    }
                                    """.formatted(employeeId, index, statuses[index])))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.empID").value(employeeId))
                    .andExpect(jsonPath("$.empStatus").value(statuses[index]));
        }
    }
}
