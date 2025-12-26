package com.hutech.demo.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class AddCompanyUserRequest {
    private Long userId;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    private String roleInCompany = "Recruiter"; // Mặc định là Recruiter
}
