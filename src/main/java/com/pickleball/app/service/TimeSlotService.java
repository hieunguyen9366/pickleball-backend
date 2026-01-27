package com.pickleball.app.service;

import com.pickleball.app.dto.court.TimeSlotDTO;
import com.pickleball.app.entity.TimeSlot;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {
    /**
     * Tạo time slots cho một sân trong một ngày
     * @param courtId ID của sân
     * @param date Ngày cần tạo slots
     * @param startHour Giờ bắt đầu (VD: 5)
     * @param endHour Giờ kết thúc (VD: 22)
     * @param slotDurationMinutes Độ dài mỗi slot (phút): 30 hoặc 60
     * @return Danh sách time slots đã tạo
     */
    List<TimeSlot> generateTimeSlotsForDate(Long courtId, LocalDate date, 
                                            int startHour, int endHour, 
                                            int slotDurationMinutes);

    /**
     * Lấy danh sách time slots của một sân trong một ngày
     */
    List<TimeSlotDTO> getTimeSlotsForDate(Long courtId, LocalDate date);

    /**
     * Lấy danh sách time slots còn trống
     */
    List<TimeSlotDTO> getAvailableTimeSlotsForDate(Long courtId, LocalDate date);

    /**
     * Cập nhật giá cho một time slot
     */
    TimeSlot updateSlotPrice(Long slotId, java.math.BigDecimal price);

    /**
     * Đánh dấu time slot là đã được đặt
     */
    TimeSlot markSlotAsBooked(Long slotId, Long bookingId);

    /**
     * Đánh dấu time slot là còn trống (khi booking bị hủy)
     */
    TimeSlot markSlotAsAvailable(Long slotId);

    /**
     * Đánh dấu nhiều time slots là đã được đặt
     */
    List<TimeSlot> markSlotsAsBooked(List<Long> slotIds, Long bookingId);

    /**
     * Đánh dấu nhiều time slots là còn trống
     */
    List<TimeSlot> markSlotsAsAvailable(List<Long> slotIds);

    /**
     * Xóa tất cả time slots của một sân trong một ngày
     */
    void deleteTimeSlotsForDate(Long courtId, LocalDate date);
}



