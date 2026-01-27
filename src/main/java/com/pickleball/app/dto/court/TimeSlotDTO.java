package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {
    private Long slotId;
    private String time;
    private String endTime;
    private boolean available;
    private BigDecimal price;
    private Boolean isLocked; // true nếu đang bị lock
    private Long lockedByUserId; // ID của user đang giữ lock, null nếu không bị lock
}



