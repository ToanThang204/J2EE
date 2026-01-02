package com.hutech.demo.controller;

import com.hutech.demo.dto.request.AiMatchRequest;
import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.User;
import com.hutech.demo.service.AiAnalysisService;
import com.hutech.demo.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý AI CV matching
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    /**
     * POST /api/ai/check-match
     * Phân tích độ phù hợp giữa CV và Job
     * 
     * Request body: { "jobId": 1, "resumeId": 2 }
     * Response: { "source", "match_score", "analysis", "industry_detected", "remaining_usage" }
     */
    @PostMapping("/check-match")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkMatch(
            @Valid @RequestBody AiMatchRequest request,
            HttpServletRequest httpRequest) {
        
        User currentUser = SecurityUtils.getCurrentUser() != null 
            ? SecurityUtils.getCurrentUser().getUser() : null;
        String ipAddress = getClientIp(httpRequest);
        
        log.info("AI Match request: userId={}, jobId={}, resumeId={}, ip={}", 
            currentUser != null ? currentUser.getId() : "guest", 
            request.getJobId(), 
            request.getResumeId(), 
            ipAddress);
        
        Map<String, Object> result = aiAnalysisService.checkMatch(
            request.getJobId(), 
            request.getResumeId(), 
            currentUser, 
            ipAddress
        );
        
        // Handle error response
        if (result.containsKey("error")) {
            int status = (int) result.getOrDefault("status", 500);
            String errorMessage = (String) result.get("error");
            
            return ResponseEntity.status(status)
                .body(ApiResponse.error(errorMessage, null));
        }
        
        // Success response
        return ResponseEntity.ok(ApiResponse.success("Phân tích thành công", result));
    }

    /**
     * Get client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs, get first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
