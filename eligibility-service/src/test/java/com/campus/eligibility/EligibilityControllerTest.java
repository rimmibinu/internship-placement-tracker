package com.campus.eligibility;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EligibilityControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void eligibleStudentIsAccepted() throws Exception {
        String body = "{\"cgpa\":8.5,\"backlogs\":0,\"department\":\"CSE\",\"minCgpa\":7.5,\"maxBacklogs\":1,\"allowedDepartments\":\"CSE,IT\"}";
        mvc.perform(post("/api/eligibility/check").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.checkedBy").value("eligibility-service"));
    }

    @Test
    void ineligibleStudentGetsAllReasons() throws Exception {
        String body = "{\"cgpa\":6.0,\"backlogs\":3,\"department\":\"MECH\",\"minCgpa\":7.5,\"maxBacklogs\":1,\"allowedDepartments\":\"CSE,IT\"}";
        mvc.perform(post("/api/eligibility/check").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.reasons.length()").value(3))
                .andExpect(jsonPath("$.reasons[*]", hasItem(containsString("CGPA"))));
    }

    @Test
    void missingCgpaIsRejected() throws Exception {
        mvc.perform(post("/api/eligibility/check").contentType(MediaType.APPLICATION_JSON).content("{\"backlogs\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cgpa").exists());
    }
}
