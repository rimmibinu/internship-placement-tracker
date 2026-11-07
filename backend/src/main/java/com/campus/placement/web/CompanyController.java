package com.campus.placement.web;

import com.campus.placement.dto.CompanyRequest;
import com.campus.placement.dto.CompanyResponse;
import com.campus.placement.service.CompanyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService service;

    public CompanyController(CompanyService service) { this.service = service; }

    @GetMapping
    public List<CompanyResponse> list(@RequestParam(defaultValue = "") String q) { return service.list(q); }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable Long id, @Valid @RequestBody CompanyRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
