package com.pickleball.app.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OccupancyReportDTO {
    private String courtName;
    private Double occupancyRate; // percentage
    private Long totalHoursBooked;
}
