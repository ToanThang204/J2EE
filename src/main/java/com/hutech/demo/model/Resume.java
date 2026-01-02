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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(name = "full_name")
    private String fullName;

    private String email;

    private String phone;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "LONGTEXT")
    private String photo;

    @Column(columnDefinition = "TEXT")
    private String personalInfo;

    @Column(columnDefinition = "TEXT")
    private String skillsSummary;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeEducation> educations;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeExperience> experiences;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeSkill> skills;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeCertification> certifications;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeAward> awards;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeReference> references;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeProject> projects;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeHobby> hobbies;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeActivity> activities;

    // Transient field để lưu userId tạm thời (không lưu vào DB)
    @Transient
    private Long tempUserId;

    // Helper methods for compatibility
    public Long getUserId() {
        if (user != null) {
            return user.getId();
        }
        return tempUserId; // Fallback to temp userId if user not loaded yet
    }

    // Setter để lưu userId tạm thời, Service layer sẽ load User entity đầy đủ
    public void setUserId(Long userId) {
        this.tempUserId = userId;
    }

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeExtrainfo> extraInfos;

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL)
    private ResumeHeader header;
}
