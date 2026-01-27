package com.pickleball.app.service;

import com.pickleball.app.dto.report.OccupancyReportDTO;
import com.pickleball.app.dto.report.RevenueReportDTO;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    com.pickleball.app.dto.report.DashboardStatsDTO getDashboardStats();

    List<RevenueReportDTO> getRevenueReport(LocalDate startDate, LocalDate endDate);

    List<OccupancyReportDTO> getOccupancyReport(LocalDate startDate, LocalDate endDate);

    List<com.pickleball.app.dto.court.CourtDTO> getTopCourtsReport(LocalDate startDate, LocalDate endDate, int limit);
}
