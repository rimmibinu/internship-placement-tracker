package com.campus.placement.dto;

import com.campus.placement.domain.Company;

public record CompanyResponse(Long id, String name, String industry, String website, String location, String description) {
    public static CompanyResponse from(Company c) {
        return new CompanyResponse(c.getId(), c.getName(), c.getIndustry(), c.getWebsite(), c.getLocation(), c.getDescription());
    }
}
