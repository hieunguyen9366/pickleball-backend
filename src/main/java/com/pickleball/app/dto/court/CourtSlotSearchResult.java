package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Kết quả tìm kiếm: Mỗi slot là một kết quả riêng biệt
 * Ví dụ: Tìm từ 6-9h, sân có slots 6-7, 7-8, 8-9 -> trả về 3 kết quả
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtSlotSearchResult {
    // Thông tin sân
    private Long courtId;
    private String courtName;
    private Long courtGroupId;
    private String courtGroupName;
    private String address;
    private String district;
    private String city;
    private String description;
    private String images;
    private String phone;
    private java.util.List<String> amenities;
    private Double rating;
    private Integer reviewCount;
    
    // Thông tin slot cụ thể
    private Long slotId;
    private String slotDate; // Format: YYYY-MM-DD
    private String slotStartTime; // Format: HH:mm
    private String slotEndTime; // Format: HH:mm
    private BigDecimal slotPrice; // Giá của slot này
    private Boolean isAvailable;
    private Boolean isLocked;
    private Long lockedByUserId;
}

