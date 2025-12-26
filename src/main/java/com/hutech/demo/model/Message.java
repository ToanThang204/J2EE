package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime sendAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Boolean isRead = false;

    // Helper methods for compatibility
    public Long getConversationId() {
        return conversation != null ? conversation.getId() : null;
    }

    public void setConversationId(Long conversationId) {
        if (conversationId != null) {
            Conversation conv = new Conversation();
            conv.setId(conversationId);
            this.conversation = conv;
        }
    }

    public Long getSenderId() {
        return user != null ? user.getId() : null;
    }

    public void setSenderId(Long senderId) {
        if (senderId != null) {
            User u = new User();
            u.setId(senderId);
            this.user = u;
        }
    }
}
