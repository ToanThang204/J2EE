package com.hutech.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO cho AI CV matching
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMatchResponse {
    
    /**
     * Nguồn kết quả: "ai" hoặc "cache"
     */
    private String source;
    
    /**
     * Điểm phù hợp 0-100
     */
    private Integer matchScore;
    
    /**
     * Full analysis data
     */
    private Map<String, Object> analysis;
    
    /**
     * Ngành nghề đã detect: it, marketing, sales, finance, general
     */
    private String industryDetected;
    
    /**
     * Số lượt còn lại
     */
    private Integer remainingUsage;
}
