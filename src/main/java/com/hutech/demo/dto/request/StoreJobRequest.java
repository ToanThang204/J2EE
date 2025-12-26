package com.hutech.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreJobRequest {
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    private String title;

    @NotBlank(message = "Mô tả công việc không được để trống")
    private String description;

    private String requirements;
    private String salaryRange;
    private String location;
    private String employmentType;
    private LocalDateTime expirationDate;

    @NotNull(message = "Company ID không được để trống")
    private Long companyId;

    private Long[] categoryIds;
}
