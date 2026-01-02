package com.hutech.demo.dto;

import com.hutech.demo.model.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private CompanyDto company;
    private String title;
    private String description;
    private String requirements;
    private String salaryRange;
    private String location;
    private String employmentType;
    private LocalDateTime postedDate;
    private LocalDateTime expirationDate;
    private JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
