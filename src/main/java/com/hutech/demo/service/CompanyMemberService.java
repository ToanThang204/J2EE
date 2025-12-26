package com.hutech.demo.service;

import com.hutech.demo.dto.request.AddCompanyUserRequest;
import com.hutech.demo.model.Company;
import com.hutech.demo.model.CompanyUser;
import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.CompanyStatus;
import com.hutech.demo.repository.CompanyRepository;
import com.hutech.demo.repository.CompanyUserRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompanyMemberService {
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;

    public Page<CompanyUser> getMembers(Long companyId, Long currentUserId, int page, int size) {
        // Kiểm tra company phải active
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new IllegalStateException("Công ty chưa được duyệt (active)");
        }

        // Kiểm tra quyền Owner đang active
        boolean isOwnerActive = companyUserRepository.existsByCompany_IdAndUser_IdAndRoleInCompanyAndStatus(
                companyId, currentUserId, "Owner", "active");
        
        if (!isOwnerActive) {
            throw new AccessDeniedException("Chỉ Owner (đang active) mới có quyền xem danh sách user");
        }

        PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by("roleInCompany").and(Sort.by("joinedAt").descending()));
        
        return companyUserRepository.findByCompany_Id(companyId, pageRequest);
    }

    @Transactional
    public CompanyUser addMember(Long companyId, Long currentUserId, AddCompanyUserRequest request) {
        // Kiểm tra company active
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        
        if (company.getStatus() != CompanyStatus.ACTIVE) {
            throw new IllegalStateException("Công ty chưa được duyệt (active)");
        }

        // Kiểm tra Owner active
        boolean isOwnerActive = companyUserRepository.existsByCompany_IdAndUser_IdAndRoleInCompanyAndStatus(
                companyId, currentUserId, "Owner", "active");
        
        if (!isOwnerActive) {
            throw new AccessDeniedException("Chỉ Owner (đang active) mới có quyền thêm nhân sự");
        }

        // Không cho gán role Owner
        String role = request.getRoleInCompany();
        if ("Owner".equals(role)) {
            throw new IllegalArgumentException("Không thể gán role Owner qua lời mời");
        }

        // Xử lý theo userId hoặc email
        User targetUser = null;
        
        if (request.getUserId() != null) {
            if (request.getUserId().equals(currentUserId)) {
                throw new IllegalArgumentException("Không thể mời chính mình");
            }
            
            targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        } else if (request.getEmail() != null) {
            targetUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));
            
            if (targetUser.getId().equals(currentUserId)) {
                throw new IllegalArgumentException("Không thể mời chính mình");
            }
        } else {
            throw new IllegalArgumentException("Phải cung cấp user_id hoặc email");
        }

        // Kiểm tra đã là member chưa
        if (companyUserRepository.existsByCompany_IdAndUser_Id(companyId, targetUser.getId())) {
            throw new IllegalArgumentException("Người dùng đã là thành viên của công ty này");
        }

        // Tạo lời mời
        CompanyUser companyUser = new CompanyUser();
        companyUser.setCompanyId(companyId);
        companyUser.setUserId(targetUser.getId());
        companyUser.setRoleInCompany(role != null ? role : "Recruiter");
        companyUser.setStatus("pending");
        
        return companyUserRepository.save(companyUser);
    }

    @Transactional
    public CompanyUser updateMemberRole(Long companyId, Long memberId, Long currentUserId, String newRole) {
        // Kiểm tra Owner active
        boolean isOwnerActive = companyUserRepository.existsByCompany_IdAndUser_IdAndRoleInCompanyAndStatus(
                companyId, currentUserId, "Owner", "active");
        
        if (!isOwnerActive) {
            throw new AccessDeniedException("Chỉ Owner mới có quyền sửa role");
        }

        if ("Owner".equals(newRole)) {
            throw new IllegalArgumentException("Không thể chuyển thành Owner");
        }

        CompanyUser member = companyUserRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        if (!member.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Member không thuộc công ty này");
        }

        if ("Owner".equals(member.getRoleInCompany())) {
            throw new IllegalArgumentException("Không thể sửa role của Owner");
        }

        member.setRoleInCompany(newRole);
        return companyUserRepository.save(member);
    }

    @Transactional
    public void removeMember(Long companyId, Long memberId, Long currentUserId) {
        // Kiểm tra Owner active
        boolean isOwnerActive = companyUserRepository.existsByCompany_IdAndUser_IdAndRoleInCompanyAndStatus(
                companyId, currentUserId, "Owner", "active");
        
        if (!isOwnerActive) {
            throw new AccessDeniedException("Chỉ Owner mới có quyền xóa thành viên");
        }

        CompanyUser member = companyUserRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        if (!member.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Member không thuộc công ty này");
        }

        if ("Owner".equals(member.getRoleInCompany())) {
            throw new IllegalArgumentException("Không thể xóa Owner");
        }

        companyUserRepository.delete(member);
    }
}
