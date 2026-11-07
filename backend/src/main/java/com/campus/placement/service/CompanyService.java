package com.campus.placement.service;

import com.campus.placement.domain.Company;
import com.campus.placement.dto.CompanyRequest;
import com.campus.placement.dto.CompanyResponse;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.CompanyRepository;
import com.campus.placement.repository.OpportunityRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyService {
    private final CompanyRepository companies;
    private final OpportunityRepository opportunities;

    public CompanyService(CompanyRepository companies, OpportunityRepository opportunities) {
        this.companies = companies;
        this.opportunities = opportunities;
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> list(String q) {
        return companies.search(q == null ? "" : q.trim()).stream().map(CompanyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse get(Long id) { return CompanyResponse.from(find(id)); }

    @Transactional
    public CompanyResponse create(CompanyRequest r) {
        companies.findByNameIgnoreCase(r.name().trim()).ifPresent(c -> { throw ApiException.conflict("A company with this name already exists"); });
        Company c = new Company();
        apply(c, r);
        return CompanyResponse.from(companies.save(c));
    }

    @Transactional
    public CompanyResponse update(Long id, CompanyRequest r) {
        Company c = find(id);
        companies.findByNameIgnoreCase(r.name().trim()).filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw ApiException.conflict("A company with this name already exists"); });
        apply(c, r);
        return CompanyResponse.from(c);
    }

    @Transactional
    public void delete(Long id) {
        Company c = find(id);
        if (opportunities.existsByCompanyId(id)) {
            throw ApiException.conflict("Cannot delete a company that has opportunities. Delete those first.");
        }
        companies.delete(c);
    }

    private Company find(Long id) {
        return companies.findById(id).orElseThrow(() -> ApiException.notFound("Company " + id + " not found"));
    }

    private void apply(Company c, CompanyRequest r) {
        c.setName(r.name().trim());
        c.setIndustry(r.industry());
        c.setWebsite(r.website());
        c.setLocation(r.location());
        c.setDescription(r.description());
    }
}
