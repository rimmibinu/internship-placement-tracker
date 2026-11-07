package com.campus.placement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** End-to-end API path through controller, service, JPA and an in-memory H2 database. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "app.seed=false",
        "eligibility.url=http://localhost:9",       // nothing listens here -> exercises the fallback path
        "eligibility.fallback-enabled=true"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlacementApiIntegrationTest {
    private static final String ADMIN = "tpo@campus.demo";
    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired MockMvc mvc;

    static long companyId, opportunityId, goodStudentId, weakStudentId, applicationId;

    private ResultActions send(String url, String user, String body) throws Exception {
        return mvc.perform(post(url).header("X-User-Email", user).contentType(JSON).content(body));
    }

    private long idOf(ResultActions r) throws Exception {
        return ((Number) JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test @Order(1)
    void requestsWithoutLoginAreUnauthorized() throws Exception {
        mvc.perform(get("/api/opportunities")).andExpect(status().isUnauthorized());
    }

    @Test @Order(2)
    void adminCreatesCompanyOpportunityAndStudents() throws Exception {
        companyId = idOf(send("/api/companies", ADMIN, "{\"name\":\"Acme Labs\",\"industry\":\"IT\"}").andExpect(status().isCreated()));
        String deadline = LocalDate.now().plusDays(10).toString();
        opportunityId = idOf(send("/api/opportunities", ADMIN, "{\"title\":\"Backend Intern\",\"type\":\"INTERNSHIP\",\"companyId\":" + companyId
                + ",\"minCgpa\":7.5,\"maxBacklogs\":0,\"allowedDepartments\":\"cse, it\",\"openings\":1,\"deadline\":\"" + deadline + "\"}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.allowedDepartments").value("CSE,IT")));
        goodStudentId = idOf(send("/api/students", ADMIN, student("good@campus.demo", "R1", "CSE", 8.8)).andExpect(status().isCreated()));
        weakStudentId = idOf(send("/api/students", ADMIN, student("weak@campus.demo", "R2", "CSE", 6.1)).andExpect(status().isCreated()));
    }

    @Test @Order(3)
    void serverSideValidationReturnsFieldErrors() throws Exception {
        send("/api/students", ADMIN, student("not-an-email", "R9", "CSE", 11.0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.cgpa").exists());
    }

    @Test @Order(4)
    void duplicateStudentEmailIsConflict() throws Exception {
        send("/api/students", ADMIN, student("good@campus.demo", "R10", "CSE", 8.0)).andExpect(status().isConflict());
    }

    @Test @Order(5)
    void studentCanApplyOnceAndDuplicateIsRejected() throws Exception {
        String body = "{\"studentId\":" + goodStudentId + ",\"opportunityId\":" + opportunityId + ",\"coverNote\":\"Keen to join\"}";
        applicationId = idOf(send("/api/applications", "good@campus.demo", body)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("APPLIED")));
        send("/api/applications", "good@campus.demo", body).andExpect(status().isConflict());
    }

    @Test @Order(6)
    void ineligibleStudentIsRejectedByEligibilityRules() throws Exception {
        send("/api/applications", "weak@campus.demo",
                "{\"studentId\":" + weakStudentId + ",\"opportunityId\":" + opportunityId + "}")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CGPA")));
    }

    @Test @Order(7)
    void studentCannotApplyForSomeoneElse() throws Exception {
        send("/api/applications", "weak@campus.demo",
                "{\"studentId\":" + goodStudentId + ",\"opportunityId\":" + opportunityId + "}")
                .andExpect(status().isForbidden());
    }

    @Test @Order(8)
    void studentCannotAccessAdminEndpoints() throws Exception {
        mvc.perform(get("/api/dashboard").header("X-User-Email", "good@campus.demo")).andExpect(status().isForbidden());
        mvc.perform(put("/api/applications/" + applicationId + "/status").header("X-User-Email", "good@campus.demo")
                .contentType(JSON).content("{\"status\":\"SHORTLISTED\"}")).andExpect(status().isForbidden());
    }

    @Test @Order(9)
    void adminMovesApplicationThroughValidTransitionsOnly() throws Exception {
        mvc.perform(put("/api/applications/" + applicationId + "/status").header("X-User-Email", ADMIN)
                .contentType(JSON).content("{\"status\":\"OFFERED\"}")).andExpect(status().isUnprocessableEntity());
        mvc.perform(put("/api/applications/" + applicationId + "/status").header("X-User-Email", ADMIN)
                .contentType(JSON).content("{\"status\":\"SHORTLISTED\",\"remarks\":\"Good\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test @Order(10)
    void searchFiltersOpportunities() throws Exception {
        mvc.perform(get("/api/opportunities").param("q", "backend").param("type", "INTERNSHIP").param("openOnly", "true")
                .header("X-User-Email", ADMIN)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/opportunities").param("q", "zzz-nothing").header("X-User-Email", ADMIN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test @Order(11)
    void dashboardAndJdbcReportWork() throws Exception {
        mvc.perform(get("/api/dashboard").header("X-User-Email", ADMIN)).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(1))
                .andExpect(jsonPath("$.applicationsByCompany[0].name").value("Acme Labs"));
        mvc.perform(get("/api/dashboard/department-report").header("X-User-Email", ADMIN)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("CSE"))
                .andExpect(jsonPath("$[0].students").value(2));
    }

    @Test @Order(12)
    void cannotDeleteCompanyWithOpportunities() throws Exception {
        mvc.perform(delete("/api/companies/" + companyId).header("X-User-Email", ADMIN)).andExpect(status().isConflict());
    }

    private static String student(String email, String roll, String dept, double cgpa) {
        return "{\"name\":\"Test " + roll + "\",\"email\":\"" + email + "\",\"rollNo\":\"" + roll + "\",\"department\":\"" + dept
                + "\",\"cgpa\":" + cgpa + ",\"backlogs\":0,\"graduationYear\":2026}";
    }
}
