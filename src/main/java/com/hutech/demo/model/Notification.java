package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String type;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String link;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper methods for compatibility
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(Long userId) {
        if (userId != null) {
            User u = new User();
            u.setId(userId);
            this.user = u;
        }
    }

    public String getTitle() {
        return this.type;
    }

    public void setTitle(String title) {
        this.type = title;
    }

    public void setIsRead(boolean isRead) {
        this.readAt = isRead ? LocalDateTime.now() : null;
    }

    public boolean getIsRead() {
        return this.readAt != null;
    }

    public boolean isRead() {
        return this.readAt != null;
    }
}
