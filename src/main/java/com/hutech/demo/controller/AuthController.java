package com.hutech.demo.controller;

import com.hutech.demo.dto.request.ForgotPasswordRequest;
import com.hutech.demo.dto.request.LoginRequest;
import com.hutech.demo.dto.request.RegisterRequest;
import com.hutech.demo.dto.request.ResetPasswordRequest;
import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.dto.response.AuthResponse;
import com.hutech.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Đăng ký thành công", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            // Manual validation to avoid filter-level exceptions
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email không được để trống");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new RuntimeException("Mật khẩu không được để trống");
            }

            System.out.println("🔵 Login attempt for: " + request.getEmail());
            AuthResponse response = authService.login(request);
            System.out.println("✅ Login successful for: " + request.getEmail());

            // Add JWT Cookie for SSR
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie
                    .from("auth_token", response.getToken())
                    .httpOnly(true)
                    .secure(false) // Set to true if HTTPS
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 days
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ApiResponse.success("Đăng nhập thành công", response));
        } catch (RuntimeException e) {
            System.err.println("🔴 RuntimeException during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("🔴 Unexpected exception during login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống. Vui lòng thử lại sau.", null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Gửi email lấy lại mật khẩu thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(
                    request.getEmail(),
                    request.getToken(),
                    request.getPassword(),
                    request.getPasswordConfirmation());
            return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}
