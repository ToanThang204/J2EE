package com.hutech.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO cho AI CV matching
 */
@Data
public class AiMatchRequest {
    
    @NotNull(message = "Job ID không được để trống")
    private Long jobId;
    
    @NotNull(message = "Resume ID không được để trống")
    private Long resumeId;
}
