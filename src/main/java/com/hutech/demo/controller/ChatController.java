package com.hutech.demo.controller;

import com.hutech.demo.dto.request.SendMessageRequest;
import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.*;
import com.hutech.demo.service.ChatService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    /**
     * Lấy danh sách conversations của user hiện tại
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<Conversation>>> getConversations() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Conversation> conversations = chatService.getConversationsByUserId(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Danh sách cuộc trò chuyện", conversations));
    }

    /**
     * Tạo hoặc lấy conversation từ application
     * Chỉ cho phép khi application status khác "pending"
     */
    @PostMapping("/conversations/from-application/{applicationId}")
    public ResponseEntity<ApiResponse<Conversation>> getOrCreateConversation(
            @PathVariable Long applicationId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Conversation conversation = chatService.getOrCreateConversationFromApplication(
                    applicationId, userId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Conversation đã sẵn sàng", conversation));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Lấy tin nhắn trong conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<Message>>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // Kiểm tra quyền truy cập
            if (!chatService.hasAccessToConversation(conversationId, userId)) {
                throw new AccessDeniedException("Bạn không có quyền xem conversation này");
            }
            
            List<Message> messages = chatService.getMessages(conversationId, page, size);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Danh sách tin nhắn", messages));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Gửi tin nhắn
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<ApiResponse<Message>> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody SendMessageRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            // Kiểm tra quyền truy cập
            if (!chatService.hasAccessToConversation(conversationId, userId)) {
                throw new AccessDeniedException("Bạn không có quyền gửi tin nhắn trong conversation này");
            }
            
            Message message = chatService.sendMessage(conversationId, userId, request.getContent());
            
            // TODO: Trigger WebSocket event để real-time
            // messageWebSocketService.broadcastMessage(conversationId, message);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tin nhắn đã được gửi", message));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Kiểm tra có thể chat với application này không
     */
    @GetMapping("/can-chat/{applicationId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> canChatWithApplication(
            @PathVariable Long applicationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        boolean canChat = chatService.canChatWithApplication(applicationId, userId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Kiểm tra quyền chat", Map.of("can_chat", canChat)));
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PostMapping("/conversations/{conversationId}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long conversationId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            
            if (!chatService.hasAccessToConversation(conversationId, userId)) {
                throw new AccessDeniedException("Bạn không có quyền truy cập conversation này");
            }
            
            chatService.markAsRead(conversationId, userId);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Messages marked as read", null));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}
