package com.hutech.demo.controller;

import com.hutech.demo.dto.request.UpgradeToEmployerRequest;
import com.hutech.demo.model.User;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.service.UserService;
import com.hutech.demo.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getMyProfile() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return userService.getUserById(userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        if (!SecurityUtils.isAdmin() && !id.equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn chỉ có thể xem thông tin của mình");
        }
        return userService.getUserById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!SecurityUtils.isAdmin() && !id.equals(SecurityUtils.getCurrentUserId())) {
            throw new AccessDeniedException("Bạn chỉ có thể sửa thông tin của mình");
        }
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new HashMap<String, Object>() {
                {
                    put("success", true);
                    put("message", "Xóa người dùng thành công");
                }
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {
                {
                    put("success", false);
                    put("message", e.getMessage());
                }
            });
        }
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> suspendUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setStatus(com.hutech.demo.model.enums.UserStatus.SUSPENDED);
            userService.updateUser(id, user);
            return ResponseEntity.ok(new HashMap<String, Object>() {
                {
                    put("success", true);
                    put("message", "Tạm ngưng tài khoản thành công");
                }
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {
                {
                    put("success", false);
                    put("message", e.getMessage());
                }
            });
        }
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setStatus(com.hutech.demo.model.enums.UserStatus.ACTIVE);
            userService.updateUser(id, user);
            return ResponseEntity.ok(new HashMap<String, Object>() {
                {
                    put("success", true);
                    put("message", "Kích hoạt tài khoản thành công");
                }
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {
                {
                    put("success", false);
                    put("message", e.getMessage());
                }
            });
        }
    }

    @PostMapping("/upgrade-to-employer")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Map<String, Object>> upgradeToEmployer(@Valid @RequestBody UpgradeToEmployerRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();

            // Debug logging
            System.out.println("=== Upgrade Request Debug ===");
            System.out.println("User ID: " + userId);
            System.out.println("Company Name: " + request.getCompanyName());
            System.out.println("Logo received: "
                    + (request.getLogo() != null ? "Yes (length: " + request.getLogo().length() + ")" : "No"));
            if (request.getLogo() != null) {
                System.out.println(
                        "Logo prefix: " + request.getLogo().substring(0, Math.min(50, request.getLogo().length())));
            }
            System.out.println("===========================");

            User updatedUser = userService.upgradeToEmployer(userId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Nâng cấp thành công! Bạn đã trở thành nhà tuyển dụng.");
            response.put("user", updatedUser);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
