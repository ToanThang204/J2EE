package com.hutech.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hutech.demo.model.*;
import com.hutech.demo.repository.AiMatchResultRepository;
import com.hutech.demo.repository.JobRepository;
import com.hutech.demo.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service xử lý AI CV matching business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final GeminiService geminiService;
    private final RateLimitService rateLimitService;
    private final AiMatchResultRepository aiMatchResultRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    private static final String FEATURE_NAME = "match_cv";
    private static final int CACHE_HOURS = 24;

    /**
     * Check match between job and resume with full context
     */
    @Transactional
    public Map<String, Object> checkMatch(Long jobId, Long resumeId, User currentUser, String ipAddress) {
        try {
            Long userId = currentUser != null ? currentUser.getId() : null;

            // 1. Rate Limiting
            if (userId != null) {
                if (rateLimitService.isUserRateLimitExceeded(userId, FEATURE_NAME)) {
                    return createErrorResponse("Bạn đã hết lượt sử dụng miễn phí hôm nay", 429);
                }
            } else {
                if (rateLimitService.isIpRateLimitExceeded(ipAddress, FEATURE_NAME)) {
                    return createErrorResponse("Bạn đã hết lượt sử dụng miễn phí. Vui lòng đăng nhập", 429);
                }
            }

            // 2. Load Resume
            Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume không tồn tại"));

            // 3. Check Cache
            AiMatchResult cachedResult = null;
            if (userId != null) {
                cachedResult = aiMatchResultRepository.findByUserIdAndJobIdAndResumeId(userId, jobId, resumeId)
                    .orElse(null);
            }

            if (cachedResult != null) {
                boolean isCacheValid = resume.getUpdatedAt().isBefore(cachedResult.getUpdatedAt())
                    && Duration.between(cachedResult.getCreatedAt(), LocalDateTime.now()).toHours() < CACHE_HOURS;

                if (isCacheValid) {
                    return buildCacheResponse(cachedResult, userId, ipAddress);
                }
            }

            // 4. Load Job with relations
            Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job không tồn tại"));

            // 5. Build contexts
            String jobDescription = buildJobDescription(job);
            String resumeContent = buildResumeContent(resume);

            if (jobDescription.isEmpty() || resumeContent.isEmpty()) {
                return createErrorResponse("Thiếu dữ liệu Job hoặc CV", 400);
            }

            Map<String, Object> companyContext = parseJsonSafe(job.getCompany().getCompanyContext());
            Map<String, Object> jobAiContext = parseJsonSafe(job.getAiContext());
            List<String> categoryNames = job.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toList());

            // 6. Call AI
            Map<String, Object> aiResult = geminiService.analyzeMatch(
                jobDescription,
                resumeContent,
                companyContext,
                jobAiContext,
                categoryNames
            );

            if (aiResult == null) {
                return createErrorResponse("Hệ thống AI đang bận. Vui lòng thử lại sau", 503);
            }

            String industryDetected = geminiService.getLastDetectedIndustry();
            Integer matchScore = (Integer) aiResult.get("match_score");

            // 7. Save to cache
            if (userId != null) {
                try {
                    String analysisJson = objectMapper.writeValueAsString(aiResult);
                    
                    AiMatchResult result = cachedResult != null ? cachedResult : new AiMatchResult();
                    result.setUser(currentUser);
                    result.setJob(job);
                    result.setResume(resume);
                    result.setMatchScore(matchScore);
                    result.setIndustryDetected(industryDetected);
                    result.setAnalysisData(analysisJson);
                    result.setUpdatedAt(LocalDateTime.now());
                    
                    aiMatchResultRepository.save(result);
                } catch (Exception e) {
                    log.error("Error saving AI match result", e);
                }
            }

            // 8. Log usage
            rateLimitService.logUsage(currentUser, ipAddress, FEATURE_NAME);

            // 9. Build response
            return buildAiResponse(matchScore, aiResult, industryDetected, userId, ipAddress);

        } catch (Exception e) {
            log.error("Error in checkMatch", e);
            return createErrorResponse("Lỗi hệ thống: " + e.getMessage(), 500);
        }
    }

    /**
     * Build job description text từ Job entity
     */
    private String buildJobDescription(Job job) {
        StringBuilder sb = new StringBuilder();
        
        if (job.getTitle() != null) {
            sb.append("Tiêu đề: ").append(job.getTitle()).append("\n\n");
        }
        
        if (job.getDescription() != null) {
            sb.append("Mô tả: ").append(job.getDescription()).append("\n\n");
        }
        
        if (job.getRequirements() != null) {
            sb.append("Yêu cầu: ").append(job.getRequirements()).append("\n\n");
        }
        
        if (job.getSalaryRange() != null) {
            sb.append("Mức lương: ").append(job.getSalaryRange()).append("\n\n");
        }
        
        if (job.getLocation() != null) {
            sb.append("Địa điểm: ").append(job.getLocation()).append("\n");
        }

        return sb.toString();
    }

    /**
     * Build resume content từ Resume entity
     */
    private String buildResumeContent(Resume resume) {
        StringBuilder sb = new StringBuilder();
        
        // Header info
        sb.append("=== THÔNG TIN CÁ NHÂN ===\n");
        if (resume.getFullName() != null) sb.append("Họ tên: ").append(resume.getFullName()).append("\n");
        if (resume.getEmail() != null) sb.append("Email: ").append(resume.getEmail()).append("\n");
        if (resume.getPhone() != null) sb.append("Phone: ").append(resume.getPhone()).append("\n");
        if (resume.getSummary() != null) sb.append("Mục tiêu: ").append(resume.getSummary()).append("\n");
        
        // Education
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            sb.append("\n=== HỌC VẤN ===\n");
            for (ResumeEducation edu : resume.getEducations()) {
                sb.append(edu.getDegree()).append(" - ").append(edu.getSchoolName());
                if (edu.getStartDate() != null) {
                    sb.append(" (").append(edu.getStartDate()).append(" - ")
                      .append(edu.getEndDate() != null ? edu.getEndDate() : "Hiện tại").append(")");
                }
                if (edu.getDescription() != null) {
                    sb.append("\n  ").append(edu.getDescription());
                }
                sb.append("\n");
            }
        }
        
        // Experience
        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            sb.append("\n=== KINH NGHIỆM ===\n");
            for (ResumeExperience exp : resume.getExperiences()) {
                sb.append(exp.getPosition()).append(" tại ").append(exp.getCompanyName());
                if (exp.getStartDate() != null) {
                    sb.append(" (").append(exp.getStartDate()).append(" - ")
                      .append(exp.getEndDate() != null ? exp.getEndDate() : "Hiện tại").append(")");
                }
                if (exp.getDescription() != null) {
                    sb.append("\n  ").append(exp.getDescription());
                }
                sb.append("\n");
            }
        }
        
        // Skills
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            sb.append("\n=== KỸ NĂNG ===\n");
            for (ResumeSkill skill : resume.getSkills()) {
                sb.append("- ").append(skill.getSkill()).append("\n");
            }
        }
        
        // Projects
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            sb.append("\n=== DỰ ÁN ===\n");
            for (ResumeProject proj : resume.getProjects()) {
                sb.append(proj.getProjectName());
                if (proj.getDescription() != null) {
                    sb.append(": ").append(proj.getDescription());
                }
                if (proj.getRole() != null) {
                    sb.append("\n  Vai trò: ").append(proj.getRole());
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Parse JSON string safely
     */
    private Map<String, Object> parseJsonSafe(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON: {}", json, e);
            return new HashMap<>();
        }
    }

    /**
     * Build cache response
     */
    private Map<String, Object> buildCacheResponse(AiMatchResult cached, Long userId, String ipAddress) {
        try {
            Map<String, Object> analysisData = objectMapper.readValue(
                cached.getAnalysisData(), 
                new TypeReference<Map<String, Object>>() {}
            );

            int remaining = userId != null
                ? rateLimitService.getRemainingUserQuota(userId, FEATURE_NAME)
                : rateLimitService.getRemainingIpQuota(ipAddress, FEATURE_NAME);

            Map<String, Object> response = new HashMap<>();
            response.put("source", "cache");
            response.put("match_score", cached.getMatchScore());
            response.put("analysis", analysisData);
            response.put("industry_detected", cached.getIndustryDetected());
            response.put("remaining_usage", remaining);
            return response;
        } catch (Exception e) {
            log.error("Error parsing cached analysis", e);
            return null;
        }
    }

    /**
     * Build AI response
     */
    private Map<String, Object> buildAiResponse(Integer matchScore, Map<String, Object> aiResult, 
                                                  String industryDetected, Long userId, String ipAddress) {
        int remaining = userId != null
            ? rateLimitService.getRemainingUserQuota(userId, FEATURE_NAME)
            : rateLimitService.getRemainingIpQuota(ipAddress, FEATURE_NAME);

        Map<String, Object> response = new HashMap<>();
        response.put("source", "ai");
        response.put("match_score", matchScore);
        response.put("analysis", aiResult);
        response.put("industry_detected", industryDetected);
        response.put("remaining_usage", remaining);
        return response;
    }

    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", statusCode);
        return response;
    }
}
