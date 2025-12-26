package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.CompanyUser;
import com.hutech.demo.service.CompanyInviteService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate/company-invites")
@RequiredArgsConstructor
public class CompanyInviteController {
    private final CompanyInviteService companyInviteService;

    /**
     * Lấy danh sách lời mời đang chờ
     * Không lấy các record tự nâng cấp (Owner)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<List<CompanyUser>>> getPendingInvites() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<CompanyUser> invites = companyInviteService.getPendingInvites(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách lời mời đang chờ", invites));
    }

    /**
     * Chấp nhận lời mời tham gia công ty
     * - Set status = active
     * - Set joined_at = now
     * - Chuyển user role sang employer nếu cần
     */
    @PostMapping("/{companyId}/accept")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<CompanyUser>> acceptInvite(@PathVariable Long companyId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            CompanyUser membership = companyInviteService.acceptInvite(companyId, userId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Bạn đã tham gia công ty", membership));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Từ chối lời mời
     */
    @PostMapping("/{companyId}/reject")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Void>> rejectInvite(@PathVariable Long companyId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            companyInviteService.rejectInvite(companyId, userId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Đã từ chối lời mời", null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}
