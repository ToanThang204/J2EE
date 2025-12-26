package com.hutech.demo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplyJobRequest {
    @NotNull(message = "Job ID không được để trống")
    private Long jobId;

    private Long resumeId;
    private String coverLetter;
    private String cvFileUrl;
    
    // For guest application
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
}
