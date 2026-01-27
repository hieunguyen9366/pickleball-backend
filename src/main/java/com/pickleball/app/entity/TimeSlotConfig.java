package com.pickleball.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity lưu trữ cấu hình khung giờ cho sân hoặc cụm sân
 * Priority: court > courtGroup > default
 * Nếu có config cho sân cụ thể thì dùng, nếu không thì dùng config của cụm sân
 */
@Entity
@Table(name = "time_slot_configs",
       indexes = {
           @Index(name = "idx_court_id", columnList = "court_id"),
           @Index(name = "idx_court_group_id", columnList = "court_group_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    /**
     * Sân cụ thể (nếu null thì áp dụng cho cả cụm sân)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id")
    private Court court;

    /**
     * Cụm sân (nếu null thì áp dụng cho sân cụ thể)
     * Lưu ý: Chỉ một trong hai (court hoặc courtGroup) được set, không được set cả hai
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_group_id")
    private CourtGroup courtGroup;

    /**
     * Giờ mở cửa (VD: 05:00)
     */
    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    /**
     * Giờ đóng cửa (VD: 23:00)
     */
    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    /**
     * Độ dài mỗi slot (phút): chỉ được 30 hoặc 60
     */
    @Column(name = "slot_duration", nullable = false)
    private Integer slotDuration; // 30 hoặc 60

    /**
     * Trạng thái active
     * true: Config đang được sử dụng
     * false: Config bị tắt, không tạo slot theo config này
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

