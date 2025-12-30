package com.hutech.demo.service;

import com.hutech.demo.dto.request.LoginRequest;
import com.hutech.demo.dto.request.RegisterRequest;
import com.hutech.demo.dto.response.AuthResponse;
import com.hutech.demo.dto.response.UserDto;
import com.hutech.demo.model.PasswordResetToken;
import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.model.enums.UserStatus;
import com.hutech.demo.repository.PasswordResetTokenRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Check password confirmation
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CANDIDATE);
        user.setStatus(UserStatus.ACTIVE);

        user = userRepository.save(user);

        // Send welcome email (non-blocking)
        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            // Log error but don't fail registration
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        // Generate token
        String token = jwtService.generateToken(user);

        // Create response
        UserDto userDto = convertToDto(user);
        return new AuthResponse(userDto, token, user.getProfile() == null, "/api/profile");
    }

    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email hoặc mật khẩu không đúng"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.err.println("🔴 Password mismatch for user: " + request.getEmail());
            System.err.println("   User status: " + user.getStatus());
            System.err.println("   User role: " + user.getRole());
            throw new RuntimeException("Email hoặc mật khẩu không đúng");
        }

        // Check if user is suspended
        if (user.getStatus() == UserStatus.SUSPENDED) {
            System.err.println("🔴 User account suspended: " + request.getEmail());
            throw new RuntimeException("Tài khoản đã bị tạm ngưng");
        }

        System.out.println("✅ Login successful for user: " + request.getEmail());
        System.out.println("   User status: " + user.getStatus());
        System.out.println("   User role: " + user.getRole());

        // Generate token
        String token = jwtService.generateToken(user);

        // Create response
        UserDto userDto = convertToDto(user);
        return new AuthResponse(userDto, token, user.getProfile() == null, "/api/profile");
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email này"));

        // Generate token
        String token = UUID.randomUUID().toString();

        // Save or update token
        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository.findByEmail(email);
        PasswordResetToken resetToken;
        
        if (existingToken.isPresent()) {
            resetToken = existingToken.get();
            resetToken.setToken(token);
            resetToken.setCreatedAt(LocalDateTime.now());
        } else {
            resetToken = new PasswordResetToken();
            resetToken.setEmail(email);
            resetToken.setToken(token);
        }

        passwordResetTokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user, token);
    }

    @Transactional
    public void resetPassword(String email, String token, String newPassword, String passwordConfirmation) {
        // Check password confirmation
        if (!newPassword.equals(passwordConfirmation)) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }

        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        // Verify token
        if (!resetToken.getToken().equals(token)) {
            throw new RuntimeException("Token không hợp lệ");
        }

        // Check if token expired
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token đã hết hạn");
        }

        // Update password
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token
        passwordResetTokenRepository.delete(resetToken);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
