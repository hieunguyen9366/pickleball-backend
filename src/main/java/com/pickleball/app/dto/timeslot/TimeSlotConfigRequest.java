package com.pickleball.app.dto.timeslot;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotConfigRequest {
    private Long courtId;
    private Long courtGroupId;

    @NotNull(message = "Open time is required")
    private LocalTime openTime;

    @NotNull(message = "Close time is required")
    private LocalTime closeTime;

    @NotNull(message = "Slot duration is required")
    @ValidSlotDuration(message = "Slot duration must be 30 or 60 minutes")
    private Integer slotDuration;

    @NotNull(message = "Is active is required")
    private Boolean isActive;
}
