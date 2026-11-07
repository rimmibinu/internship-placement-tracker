package com.campus.placement.repository;

import com.campus.placement.domain.Application;
import com.campus.placement.domain.ApplicationStatus;
import com.campus.placement.domain.OpportunityType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long>, JpaSpecificationExecutor<Application> {
    boolean existsByStudentIdAndOpportunityId(Long studentId, Long opportunityId);
    boolean existsByStudentId(Long studentId);
    boolean existsByOpportunityId(Long opportunityId);
    long countByOpportunityId(Long opportunityId);
    long countByOpportunityIdAndStatus(Long opportunityId, ApplicationStatus status);
    long countByStatus(ApplicationStatus status);
    boolean existsByStudentIdAndStatusAndOpportunity_Type(Long studentId, ApplicationStatus status, OpportunityType type);

    /** Join + group query: applications per status. */
    @Query("select a.status, count(a) from Application a group by a.status")
    List<Object[]> countGroupedByStatus();

    /** Join query across three tables: applications per company. */
    @Query("select c.name, count(a) from Application a join a.opportunity o join o.company c "
            + "group by c.name order by count(a) desc")
    List<Object[]> countGroupedByCompany();

    @Query("select count(distinct a.student.id) from Application a "
            + "where a.status = com.campus.placement.domain.ApplicationStatus.OFFERED "
            + "and a.opportunity.type = com.campus.placement.domain.OpportunityType.PLACEMENT")
    long countStudentsPlaced();
}
