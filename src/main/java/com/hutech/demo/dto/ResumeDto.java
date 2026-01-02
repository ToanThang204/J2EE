package com.hutech.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ResumeDto {
    private Long id;
    private Long userId;
    private String title;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String summary;
    private String photo;
    private String personalInfo;
    private String skillsSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
