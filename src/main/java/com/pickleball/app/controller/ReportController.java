package com.pickleball.app.controller;

import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.report.OccupancyReportDTO;
import com.pickleball.app.dto.report.RevenueReportDTO;
import com.pickleball.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<com.pickleball.app.dto.report.DashboardStatsDTO>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDashboardStats()));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<List<RevenueReportDTO>>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getRevenueReport(startDate, endDate)));
    }

    @GetMapping("/occupancy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<List<OccupancyReportDTO>>> getOccupancyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getOccupancyReport(startDate, endDate)));
    }

    @GetMapping("/top-courts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COURT_MANAGER')")
    public ResponseEntity<ApiResponse<List<com.pickleball.app.dto.court.CourtDTO>>> getTopCourtsReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getTopCourtsReport(startDate, endDate, limit)));
    }
}
