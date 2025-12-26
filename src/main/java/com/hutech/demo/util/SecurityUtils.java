package com.hutech.demo.util;

import com.hutech.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    /**
     * Lấy user đang đăng nhập
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        
        return null;
    }

    /**
     * Lấy ID của user đang đăng nhập
     */
    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUserId() : null;
    }

    /**
     * Lấy email của user đang đăng nhập
     */
    public static String getCurrentUserEmail() {
        CustomUserDetails userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUsername() : null;
    }

    /**
     * Kiểm tra user có role hay không
     */
    public static boolean hasRole(String role) {
        CustomUserDetails userDetails = getCurrentUser();
        if (userDetails == null) {
            return false;
        }
        
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Kiểm tra user có phải ADMIN không
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Kiểm tra user có phải EMPLOYER không
     */
    public static boolean isEmployer() {
        return hasRole("EMPLOYER");
    }

    /**
     * Kiểm tra user có phải CANDIDATE không
     */
    public static boolean isCandidate() {
        return hasRole("CANDIDATE");
    }
}
