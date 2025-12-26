package com.hutech.demo.controller;

import com.hutech.demo.dto.request.AddCompanyUserRequest;
import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.CompanyUser;
import com.hutech.demo.service.CompanyMemberService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employer/companies/{companyId}/members")
@RequiredArgsConstructor
public class CompanyMemberController {
    private final CompanyMemberService companyMemberService;

    /**
     * Lấy danh sách thành viên công ty
     * Chỉ Owner (đang active) mới xem được
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<CompanyUser>>> getMembers(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Page<CompanyUser> members = companyMemberService.getMembers(companyId, userId, page, size);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Danh sách nhân sự của công ty", members));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Thêm thành viên mới vào công ty
     * Chỉ Owner đang active mới được thêm
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CompanyUser>> addMember(
            @PathVariable Long companyId,
            @Valid @RequestBody AddCompanyUserRequest request) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            CompanyUser companyUser = companyMemberService.addMember(companyId, currentUserId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Đã gửi lời mời thành công", companyUser));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Cập nhật role của thành viên
     * Chỉ Owner mới được sửa
     */
    @PutMapping("/{memberId}/role")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CompanyUser>> updateMemberRole(
            @PathVariable Long companyId,
            @PathVariable Long memberId,
            @RequestParam String role) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            CompanyUser updated = companyMemberService.updateMemberRole(
                    companyId, memberId, currentUserId, role);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật role thành công", updated));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Xóa thành viên khỏi công ty
     * Chỉ Owner mới được xóa
     */
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long companyId,
            @PathVariable Long memberId) {
        try {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            companyMemberService.removeMember(companyId, memberId, currentUserId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Đã xóa thành viên khỏi công ty", null));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}
