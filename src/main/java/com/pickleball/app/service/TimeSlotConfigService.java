package com.pickleball.app.service;

import com.pickleball.app.dto.timeslot.TimeSlotConfigDTO;
import com.pickleball.app.dto.timeslot.TimeSlotConfigRequest;

import java.util.List;

public interface TimeSlotConfigService {
    /**
     * Lấy danh sách configs
     */
    List<TimeSlotConfigDTO> getConfigs(Long courtId, Long courtGroupId);

    /**
     * Lấy config theo ID
     */
    TimeSlotConfigDTO getConfigById(Long configId);

    /**
     * Tạo config mới
     */
    TimeSlotConfigDTO createConfig(TimeSlotConfigRequest request);

    /**
     * Cập nhật config
     */
    TimeSlotConfigDTO updateConfig(Long configId, TimeSlotConfigRequest request);

    /**
     * Xóa config
     */
    void deleteConfig(Long configId);

    /**
     * Lấy config cho một sân (ưu tiên config của sân, nếu không có thì lấy config của cụm sân)
     */
    TimeSlotConfigDTO getConfigForCourt(Long courtId);
}

