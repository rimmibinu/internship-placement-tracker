package com.campus.placement.repository;

import com.campus.placement.domain.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByNameIgnoreCase(String name);

    @Query("select c from Company c where lower(c.name) like lower(concat('%', :q, '%')) "
            + "or lower(coalesce(c.industry, '')) like lower(concat('%', :q, '%')) order by c.name")
    List<Company> search(@Param("q") String q);
}
