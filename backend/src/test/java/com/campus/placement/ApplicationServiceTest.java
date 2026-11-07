package com.campus.placement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.campus.placement.domain.*;
import com.campus.placement.dto.ApplicationRequest;
import com.campus.placement.dto.ApplicationResponse;
import com.campus.placement.dto.EligibilityResult;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.ApplicationRepository;
import com.campus.placement.repository.OpportunityRepository;
import com.campus.placement.repository.StudentRepository;
import com.campus.placement.service.ApplicationService;
import com.campus.placement.service.EligibilityClient;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Unit tests of the business rules (mocked repositories and eligibility service). */
class ApplicationServiceTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 10);

    private StudentRepository students;
    private OpportunityRepository opportunities;
    private ApplicationRepository applications;
    private EligibilityClient eligibility;
    private ApplicationService service;
    private Student student;
    private Opportunity opp;

    @BeforeEach
    void setUp() {
        students = mock(StudentRepository.class);
        opportunities = mock(OpportunityRepository.class);
        applications = mock(ApplicationRepository.class);
        eligibility = mock(EligibilityClient.class);
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        service = new ApplicationService(students, opportunities, applications, eligibility, clock);

        student = new Student();
        student.setId(1L); student.setName("Test Student"); student.setDepartment("CSE"); student.setCgpa(8.0);
        Company company = new Company();
        company.setId(5L); company.setName("Acme");
        opp = new Opportunity();
        opp.setId(2L); opp.setTitle("Intern"); opp.setCompany(company); opp.setType(OpportunityType.INTERNSHIP);
        opp.setDeadline(TODAY.plusDays(3)); opp.setOpenings(1); opp.setStatus(OpportunityStatus.OPEN);

        when(students.findById(1L)).thenReturn(Optional.of(student));
        when(opportunities.findById(2L)).thenReturn(Optional.of(opp));
        when(eligibility.check(any(), any())).thenReturn(new EligibilityResult(true, List.of(), "test"));
        when(applications.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private Application existing(ApplicationStatus status) {
        Application a = new Application();
        a.setId(9L); a.setStudent(student); a.setOpportunity(opp); a.setStatus(status);
        when(applications.findById(9L)).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    void applySucceedsForEligibleStudentBeforeDeadline() {
        ApplicationResponse r = service.apply(new ApplicationRequest(1L, 2L, "hello"));
        assertEquals(ApplicationStatus.APPLIED, r.status());
        verify(applications).save(any(Application.class));
    }

    @Test
    void applyOnDeadlineDayIsAllowed() {
        opp.setDeadline(TODAY);
        assertDoesNotThrow(() -> service.apply(new ApplicationRequest(1L, 2L, null)));
    }

    @Test
    void applyAfterDeadlineIsRejected() {
        opp.setDeadline(TODAY.minusDays(1));
        ApiException e = assertThrows(ApiException.class, () -> service.apply(new ApplicationRequest(1L, 2L, null)));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getStatus());
        assertTrue(e.getMessage().contains("deadline"));
    }

    @Test
    void applyToClosedOpportunityIsRejected() {
        opp.setStatus(OpportunityStatus.CLOSED);
        assertThrows(ApiException.class, () -> service.apply(new ApplicationRequest(1L, 2L, null)));
    }

    @Test
    void duplicateApplicationIsConflict() {
        when(applications.existsByStudentIdAndOpportunityId(1L, 2L)).thenReturn(true);
        ApiException e = assertThrows(ApiException.class, () -> service.apply(new ApplicationRequest(1L, 2L, null)));
        assertEquals(HttpStatus.CONFLICT, e.getStatus());
    }

    @Test
    void ineligibleStudentIsRejectedWithReasons() {
        when(eligibility.check(any(), any())).thenReturn(new EligibilityResult(false, List.of("CGPA too low"), "test"));
        ApiException e = assertThrows(ApiException.class, () -> service.apply(new ApplicationRequest(1L, 2L, null)));
        assertTrue(e.getMessage().contains("CGPA too low"));
        verify(applications, never()).save(any());
    }

    @Test
    void onePlacementOfferPolicyBlocksNewPlacementApplications() {
        opp.setType(OpportunityType.PLACEMENT);
        when(applications.existsByStudentIdAndStatusAndOpportunity_Type(1L, ApplicationStatus.OFFERED, OpportunityType.PLACEMENT)).thenReturn(true);
        assertThrows(ApiException.class, () -> service.apply(new ApplicationRequest(1L, 2L, null)));
    }

    @Test
    void unknownStudentIs404() {
        ApiException e = assertThrows(ApiException.class, () -> service.apply(new ApplicationRequest(99L, 2L, null)));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
    }

    @Test
    void validStatusTransitionWorks() {
        existing(ApplicationStatus.APPLIED);
        ApplicationResponse r = service.updateStatus(9L, ApplicationStatus.SHORTLISTED, "ok");
        assertEquals(ApplicationStatus.SHORTLISTED, r.status());
    }

    @Test
    void invalidStatusTransitionIsRejected() {
        existing(ApplicationStatus.APPLIED);
        assertThrows(ApiException.class, () -> service.updateStatus(9L, ApplicationStatus.OFFERED, null));
    }

    @Test
    void adminCannotWithdrawOnBehalfOfStudent() {
        existing(ApplicationStatus.APPLIED);
        assertThrows(ApiException.class, () -> service.updateStatus(9L, ApplicationStatus.WITHDRAWN, null));
    }

    @Test
    void offerBlockedWhenAllOpeningsFilled() {
        existing(ApplicationStatus.INTERVIEW);
        when(applications.countByOpportunityIdAndStatus(2L, ApplicationStatus.OFFERED)).thenReturn(1L);
        ApiException e = assertThrows(ApiException.class, () -> service.updateStatus(9L, ApplicationStatus.OFFERED, null));
        assertTrue(e.getMessage().contains("filled"));
    }

    @Test
    void offerWorksWhenOpeningAvailable() {
        existing(ApplicationStatus.INTERVIEW);
        when(applications.countByOpportunityIdAndStatus(anyLong(), any())).thenReturn(0L);
        assertEquals(ApplicationStatus.OFFERED, service.updateStatus(9L, ApplicationStatus.OFFERED, "congrats").status());
    }

    @Test
    void studentCanWithdrawOwnApplicationButNotAnothers() {
        existing(ApplicationStatus.SHORTLISTED);
        assertThrows(ApiException.class, () -> service.withdraw(9L, 777L));
        assertEquals(ApplicationStatus.WITHDRAWN, service.withdraw(9L, 1L).status());
    }

    @Test
    void terminalApplicationCannotBeWithdrawn() {
        existing(ApplicationStatus.REJECTED);
        assertThrows(ApiException.class, () -> service.withdraw(9L, 1L));
    }
}
