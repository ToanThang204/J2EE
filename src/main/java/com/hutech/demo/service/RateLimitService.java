package com.hutech.demo.service;

import com.hutech.demo.model.AiUsageLog;
import com.hutech.demo.model.User;
import com.hutech.demo.repository.AiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service xử lý rate limiting cho AI features
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private final AiUsageLogRepository aiUsageLogRepository;

    // Constants
    private static final int USER_DAILY_LIMIT = 100;
    private static final int IP_DAILY_LIMIT = 2;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1440; // 24 hours

    /**
     * Kiểm tra user có vượt rate limit không
     */
    public boolean isUserRateLimitExceeded(Long userId, String featureName) {
        LocalDateTime after = LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES);
        long count = aiUsageLogRepository.countByUserIdAndFeatureNameAndCreatedAtAfter(userId, featureName, after);
        return count >= USER_DAILY_LIMIT;
    }

    /**
     * Kiểm tra IP có vượt rate limit không (cho guest users)
     */
    public boolean isIpRateLimitExceeded(String ipAddress, String featureName) {
        LocalDateTime after = LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES);
        long count = aiUsageLogRepository.countByIpAddressAndFeatureNameAndCreatedAtAfter(ipAddress, featureName, after);
        return count >= IP_DAILY_LIMIT;
    }

    /**
     * Lấy số lượt còn lại cho user
     */
    public int getRemainingUserQuota(Long userId, String featureName) {
        LocalDateTime after = LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES);
        long used = aiUsageLogRepository.countByUserIdAndFeatureNameAndCreatedAtAfter(userId, featureName, after);
        return Math.max(0, USER_DAILY_LIMIT - (int) used);
    }

    /**
     * Lấy số lượt còn lại cho IP
     */
    public int getRemainingIpQuota(String ipAddress, String featureName) {
        LocalDateTime after = LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES);
        long used = aiUsageLogRepository.countByIpAddressAndFeatureNameAndCreatedAtAfter(ipAddress, featureName, after);
        return Math.max(0, IP_DAILY_LIMIT - (int) used);
    }

    /**
     * Log usage
     */
    @Transactional
    public void logUsage(User user, String ipAddress, String featureName) {
        AiUsageLog usageLog = new AiUsageLog();
        usageLog.setUser(user);
        usageLog.setIpAddress(ipAddress);
        usageLog.setFeatureName(featureName);
        aiUsageLogRepository.save(usageLog);
        
        log.debug("Logged AI usage: user={}, ip={}, feature={}", 
            user != null ? user.getId() : "null", ipAddress, featureName);
    }
}
