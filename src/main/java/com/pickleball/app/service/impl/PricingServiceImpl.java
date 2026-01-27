package com.pickleball.app.service.impl;

import com.pickleball.app.entity.CourtPriceConfig;
import com.pickleball.app.repository.CourtPriceConfigRepository;
import com.pickleball.app.service.PricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final CourtPriceConfigRepository priceConfigRepository;

    @Override
    public BigDecimal calculateSlotPrice(
            Long courtId,
            LocalDate date,
            LocalTime startTime,
            BigDecimal basePrice,
            int durationMinutes) {
        // 1. Get day of week (MON, TUE, WED, etc.)
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String dayStr = dayOfWeek.toString().substring(0, 3); // MON, TUE, WED, etc.

        // 2. Find matching price config
        List<CourtPriceConfig> configs = priceConfigRepository.findByCourt_CourtId(courtId);

        BigDecimal modifier = BigDecimal.ONE; // Default 1.0 (no change)

        for (CourtPriceConfig config : configs) {
            // Check if day matches
            if (config.getDaysOfWeek() != null && config.getDaysOfWeek().contains(dayStr)) {
                // Check if time is in range
                if (isTimeInRange(startTime, config.getTimeStart(), config.getTimeEnd())) {
                    modifier = config.getPriceModifier();
                    log.debug("Applied price modifier {} for court {} at {} on {}",
                            modifier, courtId, startTime, dayStr);
                    break; // Use first matching config
                }
            }
        }

        // 3. Calculate price
        // hourlyPrice = basePrice * modifier
        BigDecimal hourlyPrice = basePrice.multiply(modifier);

        // slotPrice = hourlyPrice * (durationMinutes / 60)
        BigDecimal slotPrice = hourlyPrice.multiply(
                BigDecimal.valueOf(durationMinutes)
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));

        return slotPrice.setScale(0, RoundingMode.HALF_UP); // Round to whole number
    }

    /**
     * Check if time is within range [start, end)
     */
    private boolean isTimeInRange(LocalTime time, LocalTime start, LocalTime end) {
        // time >= start && time < end
        return !time.isBefore(start) && time.isBefore(end);
    }
}
