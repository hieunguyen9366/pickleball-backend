package com.pickleball.app.dto.report;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class RevenueReportDTO {
    private LocalDate date; // Or Month/Year
    private BigDecimal totalRevenue;
    private Long totalBookings;
}
