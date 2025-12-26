package com.hutech.demo.repository;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByStatus(CompanyStatus status);
    List<Company> findByCompanyNameContaining(String name);
}
