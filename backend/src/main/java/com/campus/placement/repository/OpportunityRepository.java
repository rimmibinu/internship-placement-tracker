package com.campus.placement.repository;

import com.campus.placement.domain.Opportunity;
import com.campus.placement.domain.OpportunityStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {
    boolean existsByCompanyId(Long companyId);
    long countByStatusAndDeadlineGreaterThanEqual(OpportunityStatus status, LocalDate date);
    List<Opportunity> findByStatusAndDeadlineBetweenOrderByDeadline(OpportunityStatus status, LocalDate from, LocalDate to);
}
