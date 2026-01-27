package com.pickleball.app.repository;

import com.pickleball.app.entity.TimeSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

        /**
         * Tìm tất cả time slots của một sân trong một ngày
         */
        List<TimeSlot> findByCourt_CourtIdAndSlotDateOrderByStartTime(Long courtId, LocalDate date);

        /**
         * Tìm time slots còn trống của một sân trong một ngày
         */
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.court.courtId = :courtId " +
                        "AND ts.slotDate = :date AND ts.isAvailable = true " +
                        "ORDER BY ts.startTime")
        List<TimeSlot> findAvailableSlotsByCourtAndDate(
                        @Param("courtId") Long courtId,
                        @Param("date") LocalDate date);

        /**
         * Tìm time slot cụ thể
         */
        Optional<TimeSlot> findByCourt_CourtIdAndSlotDateAndStartTimeAndEndTime(
                        Long courtId,
                        LocalDate date,
                        LocalTime startTime,
                        LocalTime endTime);

        /**
         * Tìm time slots đã được đặt bởi một booking
         */
        List<TimeSlot> findByBooking_BookingId(Long bookingId);

        /**
         * Tìm time slots trong khoảng thời gian
         */
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.court.courtId = :courtId " +
                        "AND ts.slotDate = :date " +
                        "AND ts.startTime < :endTime AND ts.endTime > :startTime " +
                        "ORDER BY ts.startTime")
        List<TimeSlot> findOverlappingSlots(
                        @Param("courtId") Long courtId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * Tìm time slots trong khoảng thời gian với PESSIMISTIC LOCK để tránh race
         * condition
         * Sử dụng PESSIMISTIC_WRITE lock để đảm bảo chỉ 1 transaction có thể update
         * cùng lúc
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.court.courtId = :courtId " +
                        "AND ts.slotDate = :date " +
                        "AND ts.startTime < :endTime AND ts.endTime > :startTime " +
                        "ORDER BY ts.startTime")
        List<TimeSlot> findOverlappingSlotsWithLock(
                        @Param("courtId") Long courtId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * Đếm số lượng time slots còn trống trong một ngày
         */
        @Query("SELECT COUNT(ts) FROM TimeSlot ts WHERE ts.court.courtId = :courtId " +
                        "AND ts.slotDate = :date AND ts.isAvailable = true")
        Long countAvailableSlotsByCourtAndDate(
                        @Param("courtId") Long courtId,
                        @Param("date") LocalDate date);

        /**
         * Kiểm tra xem có booking nào trong ngày không
         */
        boolean existsByCourt_CourtIdAndSlotDateAndBookingIsNotNull(Long courtId, LocalDate slotDate);

        /**
         * Xóa tất cả time slots của một sân trong một ngày
         */
        void deleteByCourt_CourtIdAndSlotDate(Long courtId, LocalDate date);
}
