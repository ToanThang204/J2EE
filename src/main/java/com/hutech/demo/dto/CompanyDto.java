package com.hutech.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    // Keep both 'name' and 'companyName' to be compatible with existing templates
    private String name;
    private String companyName;
    private String description;
    private String logo;
    private String address;
    private String website;
    private String industry;
    private Integer employeeCount;
}
