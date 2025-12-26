package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping("/check-match")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkMatch(
            @RequestParam Long jobId,
            @RequestParam Long resumeId,
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        
        String ipAddress = request.getRemoteAddr();
        
        Map<String, Object> result = aiAnalysisService.checkMatch(jobId, resumeId, userId, ipAddress);
        
        if (result.containsKey("error")) {
            int status = (int) result.getOrDefault("status", 500);
            return ResponseEntity.status(status)
                    .body(ApiResponse.error((String) result.get("error"), null));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Phân tích thành công", result));
    }
}
