package com.pickleball.app.service.impl;

import com.pickleball.app.dto.court.TimeSlotDTO;
import com.pickleball.app.dto.timeslot.TimeSlotConfigDTO;
import com.pickleball.app.entity.Booking;
import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.TimeSlot;
import com.pickleball.app.repository.BookingRepository;
import com.pickleball.app.repository.CourtRepository;
import com.pickleball.app.repository.TimeSlotRepository;
import com.pickleball.app.service.PricingService;
import com.pickleball.app.service.TimeSlotConfigService;
import com.pickleball.app.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final TimeSlotConfigService timeSlotConfigService;
    private final PricingService pricingService;

    @Override
    @Transactional
    public List<TimeSlot> generateTimeSlotsForDate(Long courtId, LocalDate date,
            int startHour, int endHour,
            int slotDurationMinutes) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        // Nếu không có tham số, lấy từ config
        if (startHour == 0 && endHour == 0 && slotDurationMinutes == 0) {
            try {
                TimeSlotConfigDTO config = timeSlotConfigService.getConfigForCourt(courtId);
                if (config != null && config.getIsActive()) {
                    startHour = config.getOpenTime().getHour();
                    endHour = config.getCloseTime().getHour();
                    slotDurationMinutes = config.getSlotDuration();
                } else {
                    // Default config
                    startHour = 5;
                    endHour = 22;
                    slotDurationMinutes = 60;
                }
            } catch (Exception e) {
                // Nếu không tìm thấy config, dùng default
                startHour = 5;
                endHour = 22;
                slotDurationMinutes = 60;
            }
        }

        // Xóa các slots cũ nếu có (để tạo lại)
        timeSlotRepository.deleteByCourt_CourtIdAndSlotDate(courtId, date);

        List<TimeSlot> slots = new ArrayList<>();
        BigDecimal basePrice = court.getBasePricePerHour();

        for (int hour = startHour; hour < endHour; hour++) {
            int minutes = 0;
            while (minutes < 60) {
                LocalTime startTime = LocalTime.of(hour, minutes);
                LocalTime endTime = startTime.plusMinutes(slotDurationMinutes);

                // Nếu vượt quá endHour, dừng lại
                if (endTime.getHour() > endHour ||
                        (endTime.getHour() == endHour && endTime.getMinute() > 0)) {
                    break;
                }

                // Calculate price with dynamic pricing applied
                BigDecimal slotPrice = pricingService.calculateSlotPrice(
                        courtId,
                        date,
                        startTime,
                        basePrice,
                        slotDurationMinutes);

                TimeSlot slot = TimeSlot.builder()
                        .court(court)
                        .slotDate(date)
                        .startTime(startTime)
                        .endTime(endTime)
                        .price(slotPrice)
                        .isAvailable(true)
                        .booking(null)
                        .build();

                slots.add(slot);
                minutes += slotDurationMinutes;
            }
        }

        return timeSlotRepository.saveAll(slots);
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsForDate(Long courtId, LocalDate date) {
        List<TimeSlot> slots = timeSlotRepository.findByCourt_CourtIdAndSlotDateOrderByStartTime(courtId, date);
        return mapToDTO(slots);
    }

    @Override
    public List<TimeSlotDTO> getAvailableTimeSlotsForDate(Long courtId, LocalDate date) {
        List<TimeSlot> slots = timeSlotRepository.findAvailableSlotsByCourtAndDate(courtId, date);
        return mapToDTO(slots);
    }

    @Override
    @Transactional
    public TimeSlot updateSlotPrice(Long slotId, BigDecimal price) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
        slot.setPrice(price);
        return timeSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public TimeSlot markSlotAsBooked(Long slotId, Long bookingId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        slot.setIsAvailable(false);
        slot.setBooking(booking);
        return timeSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public TimeSlot markSlotAsAvailable(Long slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));

        slot.setIsAvailable(true);
        slot.setBooking(null);
        return timeSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public List<TimeSlot> markSlotsAsBooked(List<Long> slotIds, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        List<TimeSlot> slots = timeSlotRepository.findAllById(slotIds);
        slots.forEach(slot -> {
            slot.setIsAvailable(false);
            slot.setBooking(booking);
        });
        return timeSlotRepository.saveAll(slots);
    }

    @Override
    @Transactional
    public List<TimeSlot> markSlotsAsAvailable(List<Long> slotIds) {
        List<TimeSlot> slots = timeSlotRepository.findAllById(slotIds);
        slots.forEach(slot -> {
            slot.setIsAvailable(true);
            slot.setBooking(null);
        });
        return timeSlotRepository.saveAll(slots);
    }

    @Override
    @Transactional
    public void deleteTimeSlotsForDate(Long courtId, LocalDate date) {
        timeSlotRepository.deleteByCourt_CourtIdAndSlotDate(courtId, date);
    }

    private List<TimeSlotDTO> mapToDTO(List<TimeSlot> slots) {
        return slots.stream().map(slot -> TimeSlotDTO.builder()
                .slotId(slot.getSlotId())
                .time(slot.getStartTime().toString())
                .endTime(slot.getEndTime().toString())
                .available(slot.getIsAvailable())
                .price(slot.getPrice())
                .build())
                .collect(Collectors.toList());
    }
}
