package com.pickleball.app.service.impl;

import com.pickleball.app.dto.report.OccupancyReportDTO;
import com.pickleball.app.dto.report.RevenueReportDTO;
import com.pickleball.app.entity.Booking;
import com.pickleball.app.entity.Court;
import com.pickleball.app.enums.BookingStatus;
import com.pickleball.app.repository.BookingRepository;
import com.pickleball.app.repository.CourtRepository;
import com.pickleball.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

        private final BookingRepository bookingRepository;
        private final CourtRepository courtRepository;
        private final com.pickleball.app.repository.UserRepository userRepository;

        @Override
        public com.pickleball.app.dto.report.DashboardStatsDTO getDashboardStats() {
                LocalDate today = LocalDate.now();
                LocalDate startOfMonth = today.withDayOfMonth(1);

                List<Booking> allBookings = bookingRepository.findAll();
                List<Booking> paidBookings = allBookings.stream()
                                .filter(b -> b.getStatus() == BookingStatus.COMPLETED
                                                || b.getStatus() == BookingStatus.PAID
                                                || b.getStatus() == BookingStatus.CONFIRMED)
                                .collect(Collectors.toList());

                // Revenue
                BigDecimal totalRevenue = paidBookings.stream()
                                .map(Booking::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal monthRevenue = paidBookings.stream()
                                .filter(b -> !b.getBookingDate().isBefore(startOfMonth))
                                .map(Booking::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal todayRevenue = paidBookings.stream()
                                .filter(b -> b.getBookingDate().isEqual(today))
                                .map(Booking::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Bookings Count
                Long totalBookings = (long) allBookings.size();
                Long monthBookings = allBookings.stream()
                                .filter(b -> !b.getBookingDate().isBefore(startOfMonth))
                                .count();
                Long todayBookings = allBookings.stream()
                                .filter(b -> b.getBookingDate().isEqual(today))
                                .count();

                // Users
                Long totalUsers = userRepository.count();

                // Courts
                Long totalCourts = courtRepository.count();
                Long activeCourts = courtRepository.findAll().stream()
                                .filter(c -> c.getStatus() == com.pickleball.app.enums.CourtStatus.AVAILABLE)
                                .count();

                return com.pickleball.app.dto.report.DashboardStatsDTO.builder()
                                .todayRevenue(todayRevenue)
                                .monthRevenue(monthRevenue)
                                .totalRevenue(totalRevenue)
                                .todayBookings(todayBookings)
                                .monthBookings(monthBookings)
                                .totalBookings(totalBookings)
                                .newUsersThisMonth(totalUsers) // Placeholder
                                .totalUsers(totalUsers)
                                .totalCourts(totalCourts)
                                .activeCourts(activeCourts)
                                .build();
        }

        @Override
        public List<RevenueReportDTO> getRevenueReport(LocalDate startDate, LocalDate endDate) {
                List<Booking> bookings = bookingRepository.findAll().stream()
                                .filter(b -> !b.getBookingDate().isBefore(startDate)
                                                && !b.getBookingDate().isAfter(endDate))
                                .filter(b -> b.getStatus() == BookingStatus.COMPLETED
                                                || b.getStatus() == BookingStatus.PAID)
                                .collect(Collectors.toList());

                Map<LocalDate, List<Booking>> groupedByDate = bookings.stream()
                                .collect(Collectors.groupingBy(Booking::getBookingDate));

                List<RevenueReportDTO> report = new ArrayList<>();
                groupedByDate.forEach((date, list) -> {
                        BigDecimal revenue = list.stream()
                                        .map(Booking::getTotalPrice)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        report.add(RevenueReportDTO.builder()
                                        .date(date)
                                        .totalRevenue(revenue)
                                        .totalBookings((long) list.size())
                                        .build());
                });

                // Sort by date
                report.sort((a, b) -> a.getDate().compareTo(b.getDate()));

                return report;
        }

        @Override
        public List<OccupancyReportDTO> getOccupancyReport(LocalDate startDate, LocalDate endDate) {
                // Mock implementation for now as occupancy calculation is complex
                // In real app: calculate total available hours vs booked hours per court
                return new ArrayList<>();
        }

        @Override
        public List<com.pickleball.app.dto.court.CourtDTO> getTopCourtsReport(LocalDate startDate, LocalDate endDate,
                        int limit) {
                List<Booking> bookings = bookingRepository.findAll().stream()
                                .filter(b -> {
                                        if (startDate != null && b.getBookingDate().isBefore(startDate))
                                                return false;
                                        if (endDate != null && b.getBookingDate().isAfter(endDate))
                                                return false;
                                        return true;
                                })
                                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                                .collect(Collectors.toList());

                // Group by court and count bookings
                Map<Long, Long> courtBookingCounts = bookings.stream()
                                .collect(Collectors.groupingBy(
                                                b -> b.getCourt().getCourtId(),
                                                Collectors.counting()));

                // Sort by booking count and get top N
                List<com.pickleball.app.dto.court.CourtDTO> topCourts = courtBookingCounts.entrySet().stream()
                                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                                .limit(limit)
                                .map(entry -> {
                                        Court court = courtRepository.findById(entry.getKey())
                                                        .orElse(null);
                                        if (court == null) {
                                                return null;
                                        }
                                        com.pickleball.app.dto.court.CourtDTO dto = new com.pickleball.app.dto.court.CourtDTO();
                                        dto.setCourtId(court.getCourtId());
                                        dto.setCourtGroupId(court.getCourtGroup().getCourtGroupId());
                                        dto.setCourtName(court.getCourtName());
                                        dto.setStatus(court.getStatus());
                                        dto.setBasePricePerHour(court.getBasePricePerHour());
                                        dto.setDistrict(court.getCourtGroup().getDistrict());
                                        dto.setCity(court.getCourtGroup().getCity());
                                        dto.setCourtGroupName(court.getCourtGroup().getGroupName());
                                        return dto;
                                })
                                .filter(dto -> dto != null)
                                .collect(Collectors.toList());

                return topCourts;
        }
}
