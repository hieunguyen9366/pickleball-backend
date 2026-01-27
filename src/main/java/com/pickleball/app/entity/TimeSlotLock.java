package com.pickleball.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ temporary lock cho time slots
 * Lock này có thời gian hết hạn để tránh slots bị giữ quá lâu khi user đang điền form
 */
@Entity
@Table(name = "time_slot_locks",
       indexes = {
           @Index(name = "idx_slot_id", columnList = "slot_id"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_expires_at", columnList = "expires_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id")
    private Long lockId;

    /**
     * Time slot bị lock
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimeSlot timeSlot;

    /**
     * User đang giữ lock
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Thời điểm lock hết hạn
     * Sau thời điểm này, lock sẽ tự động được release
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Thời điểm lock được tạo
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

