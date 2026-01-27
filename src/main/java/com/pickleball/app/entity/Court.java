package com.pickleball.app.entity;

import com.pickleball.app.enums.CourtStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "courts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "court_id")
    private Long courtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_group_id", nullable = false)
    private CourtGroup courtGroup;

    @Column(name = "court_name", length = 50, nullable = false)
    private String courtName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourtStatus status;

    @Column(name = "base_price_per_hour", precision = 10, scale = 2)
    private BigDecimal basePricePerHour;
}
