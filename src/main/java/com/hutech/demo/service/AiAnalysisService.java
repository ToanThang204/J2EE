package com.hutech.demo.service;

import com.hutech.demo.model.AiMatchResult;
import com.hutech.demo.model.AiUsageLog;
import com.hutech.demo.model.Job;
import com.hutech.demo.model.Resume;
import com.hutech.demo.repository.AiMatchResultRepository;
import com.hutech.demo.repository.AiUsageLogRepository;
import com.hutech.demo.repository.JobRepository;
import com.hutech.demo.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final GeminiService geminiService;
    private final AiMatchResultRepository aiMatchResultRepository;
    private final AiUsageLogRepository aiUsageLogRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;

    private static final int USER_DAILY_LIMIT = 3;
    private static final int IP_DAILY_LIMIT = 2;

    /**
     * Check match between job and resume
     */
    public Map<String, Object> checkMatch(Long jobId, Long resumeId, Long userId, String ipAddress) {
        try {
            // Check rate limit
            if (userId != null) {
                if (isRateLimitExceeded(userId, "match_cv", USER_DAILY_LIMIT)) {
                    return createErrorResponse("Hết lượt dùng miễn phí (User)", 429);
                }
            } else {
                if (isIpRateLimitExceeded(ipAddress, "match_cv", IP_DAILY_LIMIT)) {
                    return createErrorResponse("Hết lượt dùng miễn phí (IP)", 429);
                }
            }

            // Check cache
            Optional<AiMatchResult> cached = aiMatchResultRepository.findByJobIdAndResumeIdAndUserId(jobId, resumeId, userId);
            if (cached.isPresent()) {
                AiMatchResult result = cached.get();
                Resume resume = resumeRepository.findById(resumeId).orElse(null);
                
                if (resume != null && !resume.getUpdatedAt().isAfter(result.getUpdatedAt()) &&
                    result.getCreatedAt().plusHours(24).isAfter(LocalDateTime.now())) {
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("source", "cache");
                    response.put("match_score", result.getMatchScore());
                    response.put("analysis", result.getAnalysisData());
                    response.put("remaining_usage", getRemainingQuota(userId, ipAddress, "match_cv"));
                    return response;
                }
            }

            // Get job and resume data
            Job job = jobRepository.findById(jobId).orElse(null);
            Resume resume = resumeRepository.findById(resumeId).orElse(null);

            if (job == null || resume == null) {
                return createErrorResponse("Job hoặc Resume không tồn tại", 404);
            }

            String jobDescription = getJobDescription(job);
            String resumeContent = getResumeContent(resume);

            // Call AI
            Map<String, Object> aiResult = geminiService.analyzeMatch(jobDescription, resumeContent);

            if (aiResult == null) {
                logUsage(userId, ipAddress, "match_cv", null, false, "AI API failed");
                return createErrorResponse("Không thể phân tích CV lúc này", 500);
            }

            // Save result
            AiMatchResult matchResult = new AiMatchResult();
            matchResult.setUser(null); // Set from userId
            matchResult.setJob(job);
            matchResult.setResume(resume);
            matchResult.setMatchScore((Integer) aiResult.get("match_score"));
            matchResult.setAnalysisData(aiResult.toString());
            matchResult.setIpAddress(ipAddress);
            aiMatchResultRepository.save(matchResult);

            // Log usage
            logUsage(userId, ipAddress, "match_cv", jobId + "-" + resumeId, true, null);

            Map<String, Object> response = new HashMap<>();
            response.put("source", "ai");
            response.put("match_score", aiResult.get("match_score"));
            response.put("analysis", aiResult);
            response.put("remaining_usage", getRemainingQuota(userId, ipAddress, "match_cv"));
            
            return response;

        } catch (Exception e) {
            log.error("Error in checkMatch", e);
            return createErrorResponse("Lỗi hệ thống", 500);
        }
    }

    private boolean isRateLimitExceeded(Long userId, String featureType, int limit) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long count = aiUsageLogRepository.countByUserIdAndFeatureTypeAndCreatedAtAfter(userId, featureType, oneDayAgo);
        return count >= limit;
    }

    private boolean isIpRateLimitExceeded(String ipAddress, String featureType, int limit) {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long count = aiUsageLogRepository.countByIpAddressAndFeatureTypeAndCreatedAtAfter(ipAddress, featureType, oneDayAgo);
        return count >= limit;
    }

    private int getRemainingQuota(Long userId, String ipAddress, String featureType) {
        if (userId != null) {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            long used = aiUsageLogRepository.countByUserIdAndFeatureTypeAndCreatedAtAfter(userId, featureType, oneDayAgo);
            return Math.max(0, USER_DAILY_LIMIT - (int) used);
        } else {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            long used = aiUsageLogRepository.countByIpAddressAndFeatureTypeAndCreatedAtAfter(ipAddress, featureType, oneDayAgo);
            return Math.max(0, IP_DAILY_LIMIT - (int) used);
        }
    }

    private void logUsage(Long userId, String ipAddress, String featureType, String requestData, boolean success, String errorMessage) {
        AiUsageLog log = new AiUsageLog();
        log.setUser(null); // Set from userId
        log.setFeatureType(featureType);
        log.setIpAddress(ipAddress);
        log.setRequestData(requestData);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        aiUsageLogRepository.save(log);
    }

    private String getJobDescription(Job job) {
        return String.format("Tiêu đề: %s\nMô tả: %s\nYêu cầu: %s",
                job.getTitle(), job.getDescription(), job.getRequirements());
    }

    private String getResumeContent(Resume resume) {
        return String.format("Tiêu đề: %s\nThông tin: %s\nKỹ năng: %s",
                resume.getTitle(), resume.getPersonalInfo(), resume.getSkillsSummary());
    }

    private Map<String, Object> createErrorResponse(String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", statusCode);
        return response;
    }
}
