package com.pickleball.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity lưu trữ các khung giờ cho thuê của sân theo ngày
 * Mỗi record đại diện cho một khung giờ cụ thể (VD: 05:00-06:00) của một sân trong một ngày
 */
@Entity
@Table(name = "time_slots", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"court_id", "slot_date", "start_time", "end_time"})
       },
       indexes = {
           @Index(name = "idx_court_date", columnList = "court_id, slot_date"),
           @Index(name = "idx_booking_id", columnList = "booking_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Long slotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Giá thuê cho khung giờ này (có thể khác với basePricePerHour của sân)
     * Nếu null, sẽ dùng basePricePerHour của sân
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Trạng thái khả dụng của slot
     * true: Còn trống, có thể đặt
     * false: Đã được đặt hoặc không khả dụng
     */
    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    /**
     * Booking ID nếu slot đã được đặt
     * null nếu slot chưa được đặt
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /**
     * Ghi chú (VD: Bảo trì, sự kiện đặc biệt, ...)
     */
    @Column(name = "notes", length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}



