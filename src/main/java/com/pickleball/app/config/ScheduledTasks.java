package com.pickleball.app.config;

import com.pickleball.app.entity.Court;
import com.pickleball.app.enums.CourtStatus;
import com.pickleball.app.repository.CourtRepository;
import com.pickleball.app.repository.TimeSlotRepository;
import com.pickleball.app.service.TimeSlotConfigService;
import com.pickleball.app.service.TimeSlotLockService;
import com.pickleball.app.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduled tasks để:
 * 1. Cleanup các locks đã hết hạn
 * 2. Tạo time slots tự động cho ngày mới
 * 3. Cleanup slots cũ
 * 4. Tự động hủy booking PENDING quá hạn
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TimeSlotLockService timeSlotLockService;
    private final TimeSlotService timeSlotService;
    private final TimeSlotConfigService timeSlotConfigService;
    private final CourtRepository courtRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final com.pickleball.app.repository.BookingRepository bookingRepository;

    /**
     * Cleanup expired locks mỗi 1 phút
     * Fixed delay: chạy sau khi task trước kết thúc 1 phút
     */
    @Scheduled(fixedDelay = 60000) // 60 seconds = 1 minute
    public void cleanupExpiredLocks() {
        log.debug("Running scheduled task: cleanup expired locks");
        try {
            timeSlotLockService.cleanupExpiredLocks();
        } catch (Exception e) {
            log.error("Error cleaning up expired locks", e);
        }
    }

    /**
     * Tạo time slots tự động cho 7 ngày tiếp theo
     * Chạy mỗi 7 ngày vào lúc 00:00 (Chủ nhật)
     */
    @Scheduled(cron = "0 0 0 * * SUN") // Mỗi Chủ nhật lúc 00:00
    public void generateTimeSlotsForFuture() {
        log.info("Starting scheduled task: Generate time slots for next 7 days");

        try {
            // Lấy tất cả courts có status = AVAILABLE
            List<Court> availableCourts = courtRepository.findAll().stream()
                    .filter(c -> c.getStatus() == CourtStatus.AVAILABLE)
                    .collect(Collectors.toList());

            int totalSlots = 0;
            int daysToGenerate = 7;

            // Generate slots for TODAY and the next 6 days (total 7 days)
            for (int i = 0; i < daysToGenerate; i++) {
                LocalDate targetDate = LocalDate.now().plusDays(i);

                for (Court court : availableCourts) {
                    // Kiểm tra xem đã có slots cho ngày này chưa
                    List<com.pickleball.app.entity.TimeSlot> existingSlots = timeSlotRepository
                            .findByCourt_CourtIdAndSlotDateOrderByStartTime(
                                    court.getCourtId(), targetDate);

                    // Nếu chưa có, tạo mới
                    if (existingSlots.isEmpty()) {
                        // Lấy config cho sân này
                        com.pickleball.app.dto.timeslot.TimeSlotConfigDTO config = timeSlotConfigService
                                .getConfigForCourt(court.getCourtId());

                        if (config != null && config.getIsActive()) {
                            int startHour = config.getOpenTime().getHour();
                            int endHour = config.getCloseTime().getHour();
                            int slotDuration = config.getSlotDuration();

                            List<com.pickleball.app.entity.TimeSlot> slots = timeSlotService.generateTimeSlotsForDate(
                                    court.getCourtId(),
                                    targetDate,
                                    startHour,
                                    endHour,
                                    slotDuration);

                            totalSlots += slots.size();
                            log.debug("Generated {} slots for court {} on {}",
                                    slots.size(), court.getCourtId(), targetDate);
                        }
                    }
                }
            }

            log.info("Completed scheduled task: Generated {} time slots for {} courts over {} days",
                    totalSlots, availableCourts.size(), daysToGenerate);
        } catch (Exception e) {
            log.error("Error generating time slots for future dates", e);
        }
    }

    /**
     * Cleanup slots cũ (quá 30 ngày)
     * Chạy mỗi ngày lúc 01:00
     */
    @Scheduled(cron = "0 0 1 * * ?") // Mỗi ngày lúc 01:00
    public void cleanupOldSlots() {
        log.info("Starting scheduled task: Cleanup old time slots");

        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(30);

            // Tìm và xóa slots cũ hơn 30 ngày
            List<com.pickleball.app.entity.TimeSlot> oldSlots = timeSlotRepository.findAll().stream()
                    .filter(slot -> slot.getSlotDate().isBefore(cutoffDate))
                    .collect(Collectors.toList());

            if (!oldSlots.isEmpty()) {
                timeSlotRepository.deleteAll(oldSlots);
                log.info("Deleted {} old time slots before {}", oldSlots.size(), cutoffDate);
            } else {
                log.debug("No old time slots to delete");
            }
        } catch (Exception e) {
            log.error("Error cleaning up old time slots", e);
        }
    }

    /**
     * Tự động hủy các booking PENDING quá hạn (15 phút)
     * Chạy mỗi phút
     */
    @Scheduled(fixedDelay = 60000) // 1 minute
    public void cancelExpiredPendingBookings() {
        log.debug("Running scheduled task: checking for expired pending bookings");
        try {
            // Hủy các booking PENDING được tạo trước 15 phút
            LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
            List<com.pickleball.app.entity.Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(
                    com.pickleball.app.enums.BookingStatus.PENDING, expirationTime);

            if (!expiredBookings.isEmpty()) {
                log.info("Found {} expired pending bookings to cancel", expiredBookings.size());

                for (com.pickleball.app.entity.Booking booking : expiredBookings) {
                    try {
                        cancelBookingSystem(booking);
                        log.info("Auto-cancelled expired booking: id={}, user={}",
                                booking.getBookingId(), booking.getUser().getUserId());
                    } catch (Exception e) {
                        log.error("Failed to auto-cancel booking {}", booking.getBookingId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in cancelExpiredPendingBookings task", e);
        }
    }

    // Helper method to cancel booking (copied logic from BookingService but without
    // user check)
    // In a real app, this should be a method in BookingService like
    // cancelBookingSystem(Long id)
    private void cancelBookingSystem(com.pickleball.app.entity.Booking booking) {
        booking.setStatus(com.pickleball.app.enums.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release time slots
        List<com.pickleball.app.entity.TimeSlot> bookedSlots = timeSlotRepository
                .findByBooking_BookingId(booking.getBookingId());

        if (!bookedSlots.isEmpty()) {
            List<Long> slotIds = bookedSlots.stream()
                    .map(com.pickleball.app.entity.TimeSlot::getSlotId)
                    .collect(Collectors.toList());
            timeSlotService.markSlotsAsAvailable(slotIds);
        }
    }
}
