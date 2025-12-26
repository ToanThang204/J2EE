package com.hutech.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreCompanyRequest {
    @NotBlank(message = "Tên công ty không được để trống")
    private String companyName;

    private String address;
    private String description;
    private String website;
    private String logo;
    private String email;
    private String phone;
}
