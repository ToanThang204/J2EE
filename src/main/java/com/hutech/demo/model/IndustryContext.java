package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity lưu cấu hình prompt AI theo ngành nghề
 * Ví dụ: IT, Marketing, Sales, Finance...
 */
@Entity
@Table(name = "industry_contexts", indexes = {
    @Index(name = "idx_key", columnList = "[key]"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndustryContext {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Key duy nhất: it, marketing, sales, finance, general
     */
    @Column(name = "[key]", nullable = false, unique = true, length = 50)
    private String key;

    /**
     * Tên hiển thị: "Công nghệ thông tin", "Marketing"...
     */
    @Column(nullable = false)
    private String name;

    /**
     * Mô tả ngành
     */
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    /**
     * JSON config: role, focus_areas, key_metrics, red_flags
     * Example:
     * {
     *   "role": "Chuyên gia Tuyển dụng IT",
     *   "focus_areas": ["Tech Stack", "Experience", "Portfolio"],
     *   "key_metrics": "Years with tech stack",
     *   "red_flags": "No hands-on experience"
     * }
     */
    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String config;

    /**
     * Có đang active không
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
