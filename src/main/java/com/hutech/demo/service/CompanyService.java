package com.hutech.demo.service;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAllWithUser();
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Company> getCompaniesByStatus(CompanyStatus status) {
        return companyRepository.findByStatusWithUser(status);
    }

    public List<Company> searchCompaniesByName(String name) {
        return companyRepository.findByCompanyNameContaining(name);
    }
    
    public List<Company> getCompaniesByUserId(Long userId) {
        return companyRepository.findByUserId(userId);
    }

    @Transactional
    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompany(Long id, Company companyDetails) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        company.setCompanyName(companyDetails.getCompanyName());
        company.setAddress(companyDetails.getAddress());
        company.setDescription(companyDetails.getDescription());
        company.setWebsite(companyDetails.getWebsite());
        company.setLogo(companyDetails.getLogo());
        company.setEmail(companyDetails.getEmail());
        company.setPhone(companyDetails.getPhone());
        company.setStatus(companyDetails.getStatus());
        
        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    @Transactional
    public Company approveCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        company.setStatus(CompanyStatus.ACTIVE);
        Company savedCompany = companyRepository.save(company);
        
        // Upgrade user to EMPLOYER role
        if (company.getUser() != null && company.getUser().getRole() != UserRole.EMPLOYER) {
            User user = company.getUser();
            user.setRole(UserRole.EMPLOYER);
            // User will be saved via cascade or manual save if needed
        }
        
        return savedCompany;
    }

    @Transactional
    public Company rejectCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        company.setStatus(CompanyStatus.REJECTED);
        return companyRepository.save(company);
    }
}
