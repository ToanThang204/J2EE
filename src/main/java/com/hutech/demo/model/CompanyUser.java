package com.hutech.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String roleInCompany; // Owner, Admin, Recruiter, Viewer

    @Column(nullable = false)
    private String status = "pending"; // pending, active, inactive

    private LocalDateTime joinedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper methods for compatibility
    public Long getCompanyId() {
        return company != null ? company.getId() : null;
    }

    public void setCompanyId(Long companyId) {
        if (companyId != null) {
            Company c = new Company();
            c.setId(companyId);
            this.company = c;
        }
    }

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
}
