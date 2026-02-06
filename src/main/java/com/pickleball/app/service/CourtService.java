package com.pickleball.app.service;

import com.pickleball.app.dto.court.CourtDTO;
import com.pickleball.app.dto.court.CourtDetailDTO;
import com.pickleball.app.dto.court.CourtGroupDTO;
import com.pickleball.app.dto.court.ImageDTO;
import com.pickleball.app.dto.court.TimeSlotDTO;

import java.util.List;

public interface CourtService {
    // Court Groups
    List<CourtGroupDTO> getAllCourtGroups();

    List<CourtGroupDTO> getCourtGroupsByManager(Long managerId);

    CourtGroupDTO createCourtGroup(CourtGroupDTO dto);

    CourtGroupDTO updateCourtGroup(Long id, CourtGroupDTO dto);

    void deleteCourtGroup(Long id);

    // Courts
    List<CourtDTO> getCourtsByGroup(Long courtGroupId);

    CourtDTO createCourt(CourtDTO dto);

    CourtDTO updateCourt(Long id, CourtDTO dto);

    void deleteCourt(Long id);

    // Advanced
    List<CourtDTO> searchCourts(String date, String startTime, String endTime); // Check availability from TimeSlot
    
    // Enhanced search with all filters
    com.pickleball.app.dto.court.CourtSearchResponse searchCourtsAdvanced(com.pickleball.app.dto.court.CourtSearchRequestDTO request);

    List<String> getCourtTimeSlots(Long courtId, String date);
    
    // New methods
    CourtDetailDTO getCourtDetailById(Long id);
    
    List<TimeSlotDTO> getAvailableTimeSlots(Long courtId, String date);
    
    boolean checkTimeSlotAvailability(Long courtId, String date, String startTime, String endTime);
    
    // Get districts and cities
    List<String> getDistricts();
    List<String> getCities();

    // Images for court groups and courts
    java.util.List<Long> uploadCourtGroupImage(Long courtGroupId, org.springframework.web.multipart.MultipartFile file);

    java.util.List<Long> uploadCourtImage(Long courtId, org.springframework.web.multipart.MultipartFile file);

    java.util.List<ImageDTO> getCourtGroupImages(Long courtGroupId);

    java.util.List<ImageDTO> getCourtImages(Long courtId);

    ImageDTO getCourtGroupImageById(Long imageId);

    ImageDTO getCourtImageById(Long imageId);
}
