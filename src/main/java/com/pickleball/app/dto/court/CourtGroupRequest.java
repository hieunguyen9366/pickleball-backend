package com.pickleball.app.dto.court;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtGroupRequest {
    
    @NotBlank(message = "Tên cụm sân là bắt buộc")
    @Size(max = 100, message = "Tên cụm sân không được vượt quá 100 ký tự")
    private String groupName;
    
    @NotBlank(message = "Địa chỉ là bắt buộc")
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;
    
    @NotBlank(message = "Quận/Huyện là bắt buộc")
    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    private String district;
    
    @NotBlank(message = "Thành phố là bắt buộc")
    @Size(max = 100, message = "Thành phố không được vượt quá 100 ký tự")
    private String city;
    
    private String description;
    
    private String images;
    
    private Long managerId;
}

