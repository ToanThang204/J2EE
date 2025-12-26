package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages;

    // Helper methods for compatibility
    public Long getUser1Id() {
        return user1 != null ? user1.getId() : null;
    }

    public void setUser1Id(Long user1Id) {
        if (user1Id != null) {
            User u = new User();
            u.setId(user1Id);
            this.user1 = u;
        }
    }

    public Long getUser2Id() {
        return user2 != null ? user2.getId() : null;
    }

    public void setUser2Id(Long user2Id) {
        if (user2Id != null) {
            User u = new User();
            u.setId(user2Id);
            this.user2 = u;
        }
    }

    public void setApplicationId(Long applicationId) {
        // Store in a field if needed for chat context
    }
}
