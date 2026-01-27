package com.pickleball.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service for calculating prices with dynamic pricing support
 */
public interface PricingService {

    /**
     * Calculate price for a specific time slot with dynamic pricing applied
     * 
     * @param courtId         Court ID
     * @param date            Slot date
     * @param startTime       Slot start time
     * @param basePrice       Court base price per hour
     * @param durationMinutes Slot duration in minutes
     * @return Calculated price with dynamic pricing modifier applied
     */
    BigDecimal calculateSlotPrice(
            Long courtId,
            LocalDate date,
            LocalTime startTime,
            BigDecimal basePrice,
            int durationMinutes);
}
