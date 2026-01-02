package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_usage_logs", indexes = {
    @Index(name = "idx_user_feature_time", columnList = "user_id, feature_name, created_at"),
    @Index(name = "idx_ip_feature_time", columnList = "ip_address, feature_name, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiUsageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "feature_name", nullable = false, length = 50)
    private String featureName; // match_cv, generate_cv, etc.

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
