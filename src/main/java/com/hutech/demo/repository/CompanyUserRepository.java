package com.hutech.demo.repository;

import com.hutech.demo.model.CompanyUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {
    List<CompanyUser> findByCompany_Id(Long companyId);
    Page<CompanyUser> findByCompany_Id(Long companyId, Pageable pageable);
    List<CompanyUser> findByUser_Id(Long userId);
    Optional<CompanyUser> findByCompany_IdAndUser_Id(Long companyId, Long userId);
    Optional<CompanyUser> findByCompany_IdAndUser_IdAndStatus(Long companyId, Long userId, String status);
    List<CompanyUser> findByUser_IdAndStatusAndRoleInCompanyNot(Long userId, String status, String roleInCompany);
    boolean existsByCompany_IdAndUser_Id(Long companyId, Long userId);
    boolean existsByCompany_IdAndUser_IdAndStatus(Long companyId, Long userId, String status);
    boolean existsByCompany_IdAndUser_IdAndRoleInCompanyAndStatus(Long companyId, Long userId, String roleInCompany, String status);
}
