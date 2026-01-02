package com.hutech.demo.repository;

import com.hutech.demo.model.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
    @Query("SELECT COUNT(a) FROM AiUsageLog a WHERE a.user.id = ?1 AND a.featureName = ?2 AND a.createdAt >= ?3")
    long countByUserIdAndFeatureNameAndCreatedAtAfter(Long userId, String featureName, LocalDateTime after);

    @Query("SELECT COUNT(a) FROM AiUsageLog a WHERE a.ipAddress = ?1 AND a.user IS NULL AND a.featureName = ?2 AND a.createdAt >= ?3")
    long countByIpAddressAndFeatureNameAndCreatedAtAfter(String ipAddress, String featureName, LocalDateTime after);
}
