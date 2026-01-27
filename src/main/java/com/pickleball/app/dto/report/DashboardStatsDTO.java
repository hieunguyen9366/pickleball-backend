package com.pickleball.app.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private BigDecimal todayRevenue;
    private BigDecimal monthRevenue;
    private BigDecimal totalRevenue;

    private Long todayBookings;
    private Long monthBookings;
    private Long totalBookings;

    private Long newUsersThisMonth;
    private Long totalUsers;

    private Long totalCourts;
    private Long activeCourts;
}
