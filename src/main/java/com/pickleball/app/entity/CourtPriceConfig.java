package com.pickleball.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "court_price_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtPriceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(name = "time_start", nullable = false)
    private LocalTime timeStart;

    @Column(name = "time_end", nullable = false)
    private LocalTime timeEnd;

    @Column(name = "days_of_week")
    private String daysOfWeek; // e.g., "MONDAY,TUESDAY" or "ALL"

    @Column(name = "price_modifier", precision = 10, scale = 2)
    private BigDecimal priceModifier;

    @Column(name = "is_holiday")
    private boolean isHoliday;
}
