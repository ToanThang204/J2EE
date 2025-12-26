package com.hutech.demo.service;

import com.hutech.demo.model.Company;
import com.hutech.demo.model.CompanyUser;
import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.repository.CompanyRepository;
import com.hutech.demo.repository.CompanyUserRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyInviteService {
    private final CompanyUserRepository companyUserRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<CompanyUser> getPendingInvites(Long userId) {
        // Chỉ lấy lời mời thực sự (không phải Owner tự nâng cấp)
        return companyUserRepository.findByUser_IdAndStatusAndRoleInCompanyNot(
                userId, "pending", "Owner");
    }

    @Transactional
    public CompanyUser acceptInvite(Long companyId, Long userId) {
        CompanyUser membership = companyUserRepository.findByCompany_IdAndUser_IdAndStatus(
                        companyId, userId, "pending")
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lời mời hoặc đã xử lý"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new IllegalStateException("Công ty chưa active");
        }

        // Cập nhật membership
        membership.setStatus("active");
        membership.setJoinedAt(LocalDateTime.now());
        companyUserRepository.save(membership);

        // Chuyển user role sang employer nếu cần
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() != UserRole.EMPLOYER && 
            (membership.getRoleInCompany().equals("Admin") || 
             membership.getRoleInCompany().equals("Recruiter"))) {
            user.setRole(UserRole.EMPLOYER);
            userRepository.save(user);
        }

        return membership;
    }

    @Transactional
    public void rejectInvite(Long companyId, Long userId) {
        CompanyUser membership = companyUserRepository.findByCompany_IdAndUser_IdAndStatus(
                        companyId, userId, "pending")
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy lời mời hoặc đã xử lý"));

        companyUserRepository.delete(membership);
    }
}
