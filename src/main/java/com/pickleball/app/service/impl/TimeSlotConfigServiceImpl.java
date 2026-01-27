package com.pickleball.app.service.impl;

import com.pickleball.app.dto.timeslot.TimeSlotConfigDTO;
import com.pickleball.app.dto.timeslot.TimeSlotConfigRequest;
import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.TimeSlotConfig;
import com.pickleball.app.repository.CourtRepository;
import com.pickleball.app.repository.CourtGroupRepository;
import com.pickleball.app.repository.TimeSlotConfigRepository;
import com.pickleball.app.service.TimeSlotConfigService;
import com.pickleball.app.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotConfigServiceImpl implements TimeSlotConfigService {

    private final TimeSlotConfigRepository configRepository;
    private final CourtRepository courtRepository;
    private final CourtGroupRepository courtGroupRepository;
    private final TimeSlotService timeSlotService;

    @Override
    public List<TimeSlotConfigDTO> getConfigs(Long courtId, Long courtGroupId) {
        List<TimeSlotConfig> configs;

        if (courtId != null) {
            Court court = courtRepository.findById(courtId)
                    .orElseThrow(() -> new RuntimeException("Court not found"));
            configs = configRepository.findByCourt(court);
        } else if (courtGroupId != null) {
            CourtGroup courtGroup = courtGroupRepository.findById(courtGroupId)
                    .orElseThrow(() -> new RuntimeException("Court group not found"));
            configs = configRepository.findByCourtGroup(courtGroup);
        } else {
            configs = configRepository.findAll();
        }

        return configs.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public TimeSlotConfigDTO getConfigById(Long configId) {
        TimeSlotConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Time slot config not found"));
        return mapToDTO(config);
    }

    @Override
    @Transactional
    public TimeSlotConfigDTO createConfig(TimeSlotConfigRequest request) {
        // Validation
        validateRequest(request);

        TimeSlotConfig config = TimeSlotConfig.builder()
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .slotDuration(request.getSlotDuration())
                .isActive(request.getIsActive())
                .build();

        if (request.getCourtId() != null) {
            Court court = courtRepository.findById(request.getCourtId())
                    .orElseThrow(() -> new RuntimeException("Court not found"));
            config.setCourt(court);
        } else if (request.getCourtGroupId() != null) {
            CourtGroup courtGroup = courtGroupRepository.findById(request.getCourtGroupId())
                    .orElseThrow(() -> new RuntimeException("Court group not found"));
            config.setCourtGroup(courtGroup);
        } else {
            throw new RuntimeException("Either courtId or courtGroupId must be provided");
        }

        TimeSlotConfig savedConfig = configRepository.save(config);

        // Regenerate slots based on new config
        triggerRegeneration(savedConfig);

        return mapToDTO(savedConfig);
    }

    @Override
    @Transactional
    public TimeSlotConfigDTO updateConfig(Long configId, TimeSlotConfigRequest request) {
        TimeSlotConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new RuntimeException("Time slot config not found"));

        // Validation
        validateRequest(request);

        config.setOpenTime(request.getOpenTime());
        config.setCloseTime(request.getCloseTime());
        config.setSlotDuration(request.getSlotDuration());
        config.setIsActive(request.getIsActive());

        TimeSlotConfig savedConfig = configRepository.save(config);

        // Regenerate slots based on updated config
        triggerRegeneration(savedConfig);

        return mapToDTO(savedConfig);
    }

    @Override
    @Transactional
    public void deleteConfig(Long configId) {
        if (!configRepository.existsById(configId)) {
            throw new RuntimeException("Time slot config not found");
        }
        configRepository.deleteById(configId);
    }

    @Override
    public TimeSlotConfigDTO getConfigForCourt(Long courtId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        // Tìm config cho sân cụ thể
        TimeSlotConfig config = configRepository.findByCourtAndIsActiveTrue(court)
                .orElse(null);

        // Nếu không có, tìm config cho cụm sân
        if (config == null && court.getCourtGroup() != null) {
            config = configRepository.findByCourtGroupAndIsActiveTrue(court.getCourtGroup())
                    .orElse(null);
        }

        // Nếu vẫn không có, trả về default config
        if (config == null) {
            return getDefaultConfig();
        }

        return mapToDTO(config);
    }

    private void validateRequest(TimeSlotConfigRequest request) {
        // Validate slot duration
        if (request.getSlotDuration() != 30 && request.getSlotDuration() != 60) {
            throw new RuntimeException("Slot duration must be 30 or 60 minutes");
        }

        // Validate close time > open time
        if (request.getCloseTime().isBefore(request.getOpenTime()) ||
                request.getCloseTime().equals(request.getOpenTime())) {
            throw new RuntimeException("Close time must be after open time");
        }

        // Validate: chỉ được set một trong hai (courtId hoặc courtGroupId)
        if (request.getCourtId() != null && request.getCourtGroupId() != null) {
            throw new RuntimeException("Cannot set both courtId and courtGroupId");
        }

        if (request.getCourtId() == null && request.getCourtGroupId() == null) {
            throw new RuntimeException("Either courtId or courtGroupId must be provided");
        }
    }

    private void triggerRegeneration(TimeSlotConfig config) {
        int daysToCheck = 30; // Check and regenerate for next 30 days

        if (config.getCourt() != null) {
            timeSlotService.regenerateTimeSlots(config.getCourt().getCourtId(), daysToCheck);
        } else if (config.getCourtGroup() != null) {
            List<Court> courts = courtRepository.findByCourtGroup(config.getCourtGroup());
            for (Court court : courts) {
                timeSlotService.regenerateTimeSlots(court.getCourtId(), daysToCheck);
            }
        }
    }

    private TimeSlotConfigDTO getDefaultConfig() {
        return TimeSlotConfigDTO.builder()
                .openTime(LocalTime.of(5, 0))
                .closeTime(LocalTime.of(22, 0))
                .slotDuration(60)
                .isActive(true)
                .build();
    }

    private TimeSlotConfigDTO mapToDTO(TimeSlotConfig config) {
        return TimeSlotConfigDTO.builder()
                .configId(config.getConfigId())
                .courtId(config.getCourt() != null ? config.getCourt().getCourtId() : null)
                .courtName(config.getCourt() != null ? config.getCourt().getCourtName() : null)
                .courtGroupId(config.getCourtGroup() != null ? config.getCourtGroup().getCourtGroupId() : null)
                .courtGroupName(config.getCourtGroup() != null ? config.getCourtGroup().getGroupName() : null)
                .openTime(config.getOpenTime())
                .closeTime(config.getCloseTime())
                .slotDuration(config.getSlotDuration())
                .isActive(config.getIsActive())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
