package com.pickleball.app.dto.timeslot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotConfigDTO {
    private Long configId;
    private Long courtId;
    private String courtName;
    private Long courtGroupId;
    private String courtGroupName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotDuration;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
