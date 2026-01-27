package com.pickleball.app.dto.common;

import com.pickleball.app.enums.UserRole;
import com.pickleball.app.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserRole role;
    private String avatarUrl;
    private UserStatus status;
}
