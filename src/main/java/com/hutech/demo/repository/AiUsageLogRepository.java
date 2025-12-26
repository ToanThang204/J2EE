package com.hutech.demo.repository;

import com.hutech.demo.model.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
    @Query("SELECT COUNT(a) FROM AiUsageLog a WHERE a.user.id = ?1 AND a.featureType = ?2 AND a.createdAt >= ?3")
    long countByUserIdAndFeatureTypeAndCreatedAtAfter(Long userId, String featureType, LocalDateTime after);

    @Query("SELECT COUNT(a) FROM AiUsageLog a WHERE a.ipAddress = ?1 AND a.featureType = ?2 AND a.createdAt >= ?3")
    long countByIpAddressAndFeatureTypeAndCreatedAtAfter(String ipAddress, String featureType, LocalDateTime after);
}
