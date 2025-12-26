package com.hutech.demo.dto.response;

import com.hutech.demo.model.enums.UserRole;
import com.hutech.demo.model.enums.UserStatus;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private UserStatus status;
}
