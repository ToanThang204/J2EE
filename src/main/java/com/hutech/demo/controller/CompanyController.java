package com.hutech.demo.controller;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.service.CompanyService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/my-company")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Company> getMyCompany() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Company> companies = companyService.getAllCompanies();
        Company company = companies.stream()
                .filter(c -> c.getUserId() != null && c.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        
        if (company == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return companyService.getCompanyById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Company>> getCompaniesByStatus(@PathVariable CompanyStatus status) {
        return ResponseEntity.ok(companyService.getCompaniesByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Company>> searchCompanies(@RequestParam String name) {
        return ResponseEntity.ok(companyService.searchCompaniesByName(name));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        company.setUserId(SecurityUtils.getCurrentUserId());
        Company createdCompany = companyService.createCompany(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCompany);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        Company existingCompany = companyService.getCompanyById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        if (!SecurityUtils.isAdmin() && !existingCompany.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa công ty này");
        }
        
        Company updatedCompany = companyService.updateCompany(id, company);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        Company existingCompany = companyService.getCompanyById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        if (!SecurityUtils.isAdmin() && !existingCompany.getUserId().equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa công ty này");
        }
        
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveCompany(@PathVariable Long id) {
        try {
            Company company = companyService.approveCompany(id);
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("success", true);
                put("message", "Duyệt công ty thành công");
                put("company", company);
            }});
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{
                put("success", false);
                put("message", e.getMessage());
            }});
        }
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectCompany(@PathVariable Long id) {
        try {
            Company company = companyService.rejectCompany(id);
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("success", true);
                put("message", "Từ chối công ty thành công");
                put("company", company);
            }});
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, Object>() {{
                put("success", false);
                put("message", e.getMessage());
            }});
        }
    }
}
