package com.hutech.demo.service;

import com.hutech.demo.model.*;
import com.hutech.demo.model.enums.ApplicationStatus;
import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;

    public List<Conversation> getConversationsByUserId(Long userId) {
        return conversationRepository.findByUser1_IdOrUser2_Id(userId, userId);
    }

    @Transactional
    public Conversation getOrCreateConversationFromApplication(Long applicationId, Long currentUserId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Kiểm tra status phải khác "pending"
        if (application.getStatus() == ApplicationStatus.PENDING) {
            throw new IllegalStateException("Không thể chat khi application đang pending");
        }

        Long candidateUserId = application.getUserId();
        
        // Xác định employerId
        Long employerId = application.getJob().getCompany().getUserId();
        
        // Kiểm tra quyền
        boolean isCandidate = currentUserId.equals(candidateUserId);
        boolean isEmployer = companyUserRepository.existsByCompany_IdAndUser_IdAndStatus(
                application.getJob().getCompany().getId(), 
                currentUserId, 
                "active");

        if (!isCandidate && !isEmployer) {
            throw new AccessDeniedException("Bạn không có quyền truy cập conversation này");
        }

        // Tìm hoặc tạo conversation
        Long user1Id = Math.min(candidateUserId, employerId);
        Long user2Id = Math.max(candidateUserId, employerId);

        return conversationRepository.findByUser1_IdAndUser2_Id(user1Id, user2Id)
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setUser1Id(user1Id);
                    conversation.setUser2Id(user2Id);
                    conversation.setApplicationId(applicationId);
                    return conversationRepository.save(conversation);
                });
    }

    public boolean hasAccessToConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        return conversation.getUser1Id().equals(userId) || 
               conversation.getUser2Id().equals(userId);
    }

    public List<Message> getMessages(Long conversationId, int page, int size) {
        // Simplified - should use Pageable
        return messageRepository.findByConversation_IdOrderByCreatedAtDesc(conversationId);
    }

    @Transactional
    public Message sendMessage(Long conversationId, Long senderId, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setIsRead(false);
        
        return messageRepository.save(message);
    }

    public boolean canChatWithApplication(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() == ApplicationStatus.PENDING) {
            return false;
        }

        Long candidateUserId = application.getUserId();
        boolean isCandidate = userId.equals(candidateUserId);
        
        boolean isEmployer = companyUserRepository.existsByCompany_IdAndUser_IdAndStatus(
                application.getJob().getCompany().getId(), 
                userId, 
                "active");

        return isCandidate || isEmployer;
    }

    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        List<Message> messages = messageRepository.findByConversation_IdAndIsReadFalse(conversationId);
        messages.stream()
                .filter(m -> !m.getSenderId().equals(userId))
                .forEach(m -> {
                    m.setIsRead(true);
                    messageRepository.save(m);
                });
    }
}
