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

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeActivity> activities;

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

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<ResumeExtrainfo> extraInfos;

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL)
    private ResumeHeader header;
}
