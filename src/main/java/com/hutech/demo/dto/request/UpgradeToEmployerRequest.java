package com.hutech.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpgradeToEmployerRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    private String website;

    private String email;

    private String phone;

    @NotBlank(message = "Mô tả công ty không được để trống")
    @Size(min = 20, message = "Mô tả phải có ít nhất 20 ký tự")
    private String description;

    private String logo; // Base64 encoded logo
}
