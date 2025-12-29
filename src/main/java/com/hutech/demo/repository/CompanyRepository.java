package com.hutech.demo.repository;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.enums.CompanyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findByStatus(CompanyStatus status);
    List<Company> findByCompanyNameContaining(String name);
    
    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.user")
    List<Company> findAllWithUser();
    
    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.user WHERE c.status = :status")
    List<Company> findByStatusWithUser(CompanyStatus status);
    
    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.user WHERE c.user.id = :userId OR (c.user IS NULL AND c.status = 'ACTIVE')")
    List<Company> findByUserId(Long userId);
    
    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.user WHERE c.status = 'ACTIVE'")
    List<Company> findActiveCompanies();
}
