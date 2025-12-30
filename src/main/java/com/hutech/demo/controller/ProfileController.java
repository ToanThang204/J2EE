package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.Profile;
import com.hutech.demo.service.ProfileService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/candidate/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    // Xem profile của chính mình
    @GetMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Profile>> getMyProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        Profile profile = profileService.getProfileByUserId(userId);

        // Convert avatar to base64 URL if exists
        if (profile != null && profile.getAvatar() != null) {
            String avatarUrl = "data:image/png;base64," + profile.getAvatar();
            profile.setAvatarUrl(avatarUrl);
        }

        return ResponseEntity.ok(ApiResponse.success("Thông tin profile", profile));
    }

    // Cập nhật profile
    @PutMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody Profile profile) {
        Long userId = SecurityUtils.getCurrentUserId();
        profile.setUserId(userId);
        profileService.updateProfile(profile);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công!", null));
    }

    // Upload avatar
    @PostMapping("/avatar")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            byte[] bytes = file.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);

            Profile profile = profileService.getProfileByUserId(userId);
            if (profile == null) {
                profile = new Profile();
                profile.setUserId(userId);
            }

            profile.setAvatar(base64);
            profileService.updateProfile(profile);

            return ResponseEntity.ok(ApiResponse.success("Upload avatar thành công!", base64));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload avatar: " + e.getMessage(), null));
        }
    }
}
