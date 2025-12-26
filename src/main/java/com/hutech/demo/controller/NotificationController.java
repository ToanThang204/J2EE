package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.Notification;
import com.hutech.demo.service.NotificationService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Lấy danh sách notifications của user hiện tại
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Notification>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Notification> notifications = notificationService.getUserNotifications(userId, page, size);
        
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách thông báo", notifications));
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        long count = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Số thông báo chưa đọc", count));
    }

    /**
     * Đánh dấu 1 notification đã đọc
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Notification>> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationService.markAsRead(id, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Đã đánh dấu đọc", notification));
    }

    /**
     * Đánh dấu tất cả đã đọc
     */
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Đã đánh dấu tất cả đã đọc", null));
    }

    /**
     * Xóa notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Đã xóa thông báo", null));
    }
}
