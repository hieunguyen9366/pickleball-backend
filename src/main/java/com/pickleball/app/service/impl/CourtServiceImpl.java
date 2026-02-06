package com.pickleball.app.service.impl;

import com.pickleball.app.dto.court.CourtDTO;
import com.pickleball.app.dto.court.CourtGroupDTO;
import com.pickleball.app.dto.court.ImageDTO;
import com.pickleball.app.dto.court.TimeSlotDTO;
import com.pickleball.app.entity.Court;
import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.CourtGroupImage;
import com.pickleball.app.entity.CourtImage;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.CourtGroupImageRepository;
import com.pickleball.app.repository.CourtGroupRepository;
import com.pickleball.app.repository.CourtImageRepository;
import com.pickleball.app.repository.CourtRepository;
import com.pickleball.app.repository.TimeSlotRepository;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.repository.ServiceRepository;
import com.pickleball.app.enums.ServiceStatus;
import com.pickleball.app.service.CourtService;
import com.pickleball.app.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements CourtService {

    private final CourtGroupRepository countGroupRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;
    private final CourtGroupImageRepository courtGroupImageRepository;
    private final CourtImageRepository courtImageRepository;
    private final TimeSlotService timeSlotService;
    private final TimeSlotRepository timeSlotRepository;
    private final ServiceRepository serviceRepository;
    private final com.pickleball.app.service.TimeSlotLockService timeSlotLockService;

    // --- Court Group ---

    @Override
    public List<CourtGroupDTO> getAllCourtGroups() {
        return countGroupRepository.findAll().stream()
                .map(this::mapToCourtGroupDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourtGroupDTO> getCourtGroupsByManager(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));
        return countGroupRepository.findByManager(manager).stream()
                .map(this::mapToCourtGroupDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourtGroupDTO createCourtGroup(CourtGroupDTO dto) {
        // Validate required fields
        if (dto.getGroupName() == null || dto.getGroupName().trim().isEmpty()) {
            throw new RuntimeException("Tên cụm sân là bắt buộc");
        }
        if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ là bắt buộc");
        }
        if (dto.getDistrict() == null || dto.getDistrict().trim().isEmpty()) {
            throw new RuntimeException("Quận/Huyện là bắt buộc");
        }
        if (dto.getCity() == null || dto.getCity().trim().isEmpty()) {
            throw new RuntimeException("Thành phố là bắt buộc");
        }

        CourtGroup group = new CourtGroup();
        group.setGroupName(dto.getGroupName().trim());
        group.setAddress(dto.getAddress().trim());
        group.setDistrict(dto.getDistrict().trim());
        group.setCity(dto.getCity().trim());
        group.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        group.setImages(dto.getImages());

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            group.setManager(manager);
        }

        CourtGroup saved = countGroupRepository.save(group);
        return mapToCourtGroupDTO(saved);
    }

    @Override
    public CourtGroupDTO updateCourtGroup(Long id, CourtGroupDTO dto) {
        CourtGroup group = countGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        // Validate required fields
        if (dto.getGroupName() == null || dto.getGroupName().trim().isEmpty()) {
            throw new RuntimeException("Tên cụm sân là bắt buộc");
        }
        if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ là bắt buộc");
        }
        if (dto.getDistrict() == null || dto.getDistrict().trim().isEmpty()) {
            throw new RuntimeException("Quận/Huyện là bắt buộc");
        }
        if (dto.getCity() == null || dto.getCity().trim().isEmpty()) {
            throw new RuntimeException("Thành phố là bắt buộc");
        }

        group.setGroupName(dto.getGroupName().trim());
        group.setAddress(dto.getAddress().trim());
        group.setDistrict(dto.getDistrict().trim());
        group.setCity(dto.getCity().trim());
        group.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        group.setImages(dto.getImages());

        if (dto.getManagerId() != null) {
            User manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
            group.setManager(manager);
        } else {
            group.setManager(null);
        }

        CourtGroup saved = countGroupRepository.save(group);
        return mapToCourtGroupDTO(saved);
    }

    @Override
    public void deleteCourtGroup(Long id) {
        countGroupRepository.deleteById(id);
    }

    // --- Court ---

    @Override
    public List<CourtDTO> getCourtsByGroup(Long courtGroupId) {
        CourtGroup group = countGroupRepository.findById(courtGroupId)
                .orElseThrow(() -> new RuntimeException("Court Group not found"));
        return courtRepository.findByCourtGroup(group).stream()
                .map(this::mapToCourtDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CourtDTO createCourt(CourtDTO dto) {
        // Validate required fields
        if (dto.getCourtGroupId() == null) {
            throw new RuntimeException("Court Group ID is required");
        }
        if (dto.getCourtName() == null || dto.getCourtName().trim().isEmpty()) {
            throw new RuntimeException("Court name is required");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required");
        }
        if (dto.getBasePricePerHour() == null || dto.getBasePricePerHour().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Base price per hour must be positive");
        }

        CourtGroup group = countGroupRepository.findById(dto.getCourtGroupId())
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        Court court = new Court();
        court.setCourtGroup(group);
        court.setCourtName(dto.getCourtName().trim());
        court.setStatus(dto.getStatus());
        court.setBasePricePerHour(dto.getBasePricePerHour());

        Court saved = courtRepository.save(court);
        return mapToCourtDTO(saved);
    }

    @Override
    public CourtDTO updateCourt(Long id, CourtDTO dto) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        // Validate required fields
        if (dto.getCourtGroupId() == null) {
            throw new RuntimeException("Court Group ID is required");
        }
        if (dto.getCourtName() == null || dto.getCourtName().trim().isEmpty()) {
            throw new RuntimeException("Court name is required");
        }
        if (dto.getStatus() == null) {
            throw new RuntimeException("Status is required");
        }
        if (dto.getBasePricePerHour() == null || dto.getBasePricePerHour().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Base price per hour must be positive");
        }

        // Update court group if changed
        if (!court.getCourtGroup().getCourtGroupId().equals(dto.getCourtGroupId())) {
            CourtGroup group = countGroupRepository.findById(dto.getCourtGroupId())
                    .orElseThrow(() -> new RuntimeException("Court Group not found"));
            court.setCourtGroup(group);
        }

        court.setCourtName(dto.getCourtName().trim());
        court.setStatus(dto.getStatus());
        court.setBasePricePerHour(dto.getBasePricePerHour());

        Court saved = courtRepository.save(court);
        return mapToCourtDTO(saved);
    }

    @Override
    public void deleteCourt(Long id) {
        courtRepository.deleteById(id);
    }

    @Override
    public com.pickleball.app.dto.court.CourtSearchResponse searchCourtsAdvanced(
            com.pickleball.app.dto.court.CourtSearchRequestDTO request) {
        
        // Get all active courts
        List<Court> allCourts = courtRepository.findAll().stream()
                .filter(c -> c.getStatus() == com.pickleball.app.enums.CourtStatus.AVAILABLE)
                .collect(Collectors.toList());
        
        // Apply search term filter
        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            String searchTerm = request.getSearchTerm().toLowerCase().trim();
            allCourts = allCourts.stream()
                    .filter(court -> {
                        String courtName = (court.getCourtName() != null) ? court.getCourtName().toLowerCase() : "";
                        String groupName = (court.getCourtGroup().getGroupName() != null) 
                                ? court.getCourtGroup().getGroupName().toLowerCase() : "";
                        String address = (court.getCourtGroup().getAddress() != null) 
                                ? court.getCourtGroup().getAddress().toLowerCase() : "";
                        String district = (court.getCourtGroup().getDistrict() != null) 
                                ? court.getCourtGroup().getDistrict().toLowerCase() : "";
                        String city = (court.getCourtGroup().getCity() != null) 
                                ? court.getCourtGroup().getCity().toLowerCase() : "";
                        
                        return courtName.contains(searchTerm) 
                                || groupName.contains(searchTerm)
                                || address.contains(searchTerm)
                                || district.contains(searchTerm)
                                || city.contains(searchTerm);
                    })
                    .collect(Collectors.toList());
        }
        
        // Apply location filters
        if (request.getDistrict() != null && !request.getDistrict().trim().isEmpty()) {
            allCourts = allCourts.stream()
                    .filter(c -> c.getCourtGroup().getDistrict() != null 
                            && c.getCourtGroup().getDistrict().equals(request.getDistrict()))
                    .collect(Collectors.toList());
        }
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            allCourts = allCourts.stream()
                    .filter(c -> c.getCourtGroup().getCity() != null 
                            && c.getCourtGroup().getCity().equals(request.getCity()))
                    .collect(Collectors.toList());
        }
        if (request.getCourtGroupId() != null) {
            allCourts = allCourts.stream()
                    .filter(c -> c.getCourtGroup().getCourtGroupId().equals(request.getCourtGroupId()))
                    .collect(Collectors.toList());
        }
        if (request.getStatus() != null) {
            allCourts = allCourts.stream()
                    .filter(c -> c.getStatus() == request.getStatus())
                    .collect(Collectors.toList());
        }
        
        // Nếu có date/time filter, trả về từng slot riêng biệt
        if (request.getDate() != null && request.getStartTime() != null && request.getEndTime() != null) {
            LocalDate searchDate = LocalDate.parse(request.getDate());
            // Normalize time format: "5:00" -> "05:00" để parse được
            String normalizedStartTime = normalizeTimeFormat(request.getStartTime());
            String normalizedEndTime = normalizeTimeFormat(request.getEndTime());
            LocalTime searchStart = LocalTime.parse(normalizedStartTime);
            LocalTime searchEnd = LocalTime.parse(normalizedEndTime);
            
            if (!searchStart.isAfter(searchEnd) && !searchStart.equals(searchEnd)) {
                // Tìm tất cả slots trong khoảng thời gian từ các courts đã filter
                List<com.pickleball.app.dto.court.CourtSlotSearchResult> slotResults = new java.util.ArrayList<>();
                
                for (Court court : allCourts) {
                    // Tìm tất cả slots overlap với khoảng thời gian tìm kiếm
                    List<com.pickleball.app.entity.TimeSlot> overlappingSlots = 
                        timeSlotRepository.findOverlappingSlots(
                            court.getCourtId(), 
                            searchDate, 
                            searchStart, 
                            searchEnd
                        );
                    
                    // Lọc chỉ các slots available và nằm trong khoảng thời gian
                    for (com.pickleball.app.entity.TimeSlot slot : overlappingSlots) {
                        // Slot phải available và chưa được đặt
                        if (!slot.getIsAvailable() || slot.getBooking() != null) {
                            continue;
                        }
                        
                        // Slot phải nằm trong khoảng thời gian tìm kiếm
                        if (slot.getStartTime().isBefore(searchStart) || slot.getEndTime().isAfter(searchEnd)) {
                            continue;
                        }
                        
                        // Map court + slot thành CourtSlotSearchResult
                        com.pickleball.app.dto.court.CourtSlotSearchResult result = 
                            mapToCourtSlotSearchResult(court, slot);
                        
                        // Check lock status
                        java.util.Map<Long, Long> lockedByUserIds = 
                            timeSlotLockService.getLockedByUserIds(java.util.Arrays.asList(slot.getSlotId()));
                        Long lockedByUserId = lockedByUserIds.get(slot.getSlotId());
                        if (lockedByUserId != null) {
                            result.setIsLocked(true);
                            result.setLockedByUserId(lockedByUserId);
                        } else {
                            result.setIsLocked(false);
                            result.setLockedByUserId(null);
                        }
                        
                        slotResults.add(result);
                    }
                }
                
                // Apply price filters on slot price
                if (request.getMinPrice() != null) {
                    slotResults = slotResults.stream()
                            .filter(r -> r.getSlotPrice() != null 
                                    && r.getSlotPrice().compareTo(request.getMinPrice()) >= 0)
                            .collect(Collectors.toList());
                }
                if (request.getMaxPrice() != null) {
                    slotResults = slotResults.stream()
                            .filter(r -> r.getSlotPrice() != null 
                                    && r.getSlotPrice().compareTo(request.getMaxPrice()) <= 0)
                            .collect(Collectors.toList());
                }
                
                // Apply rating filter
                if (request.getMinRating() != null && request.getMinRating() > 0) {
                    slotResults = slotResults.stream()
                            .filter(r -> r.getRating() != null && r.getRating() >= request.getMinRating())
                            .collect(Collectors.toList());
                }
                
                // Apply amenities filter
                if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
                    List<String> requiredAmenities = request.getAmenities();
                    slotResults = slotResults.stream()
                            .filter(r -> {
                                if (r.getAmenities() == null || r.getAmenities().isEmpty()) {
                                    return false;
                                }
                                return r.getAmenities().containsAll(requiredAmenities);
                            })
                            .collect(Collectors.toList());
                }
                
                // Apply sorting
                String sortBy = request.getSortBy() != null ? request.getSortBy() : "default";
                switch (sortBy) {
                    case "price_asc":
                        slotResults.sort((a, b) -> {
                            BigDecimal priceA = a.getSlotPrice() != null ? a.getSlotPrice() : BigDecimal.ZERO;
                            BigDecimal priceB = b.getSlotPrice() != null ? b.getSlotPrice() : BigDecimal.ZERO;
                            return priceA.compareTo(priceB);
                        });
                        break;
                    case "price_desc":
                        slotResults.sort((a, b) -> {
                            BigDecimal priceA = a.getSlotPrice() != null ? a.getSlotPrice() : BigDecimal.ZERO;
                            BigDecimal priceB = b.getSlotPrice() != null ? b.getSlotPrice() : BigDecimal.ZERO;
                            return priceB.compareTo(priceA);
                        });
                        break;
                    case "rating_desc":
                        slotResults.sort((a, b) -> {
                            Double ratingA = a.getRating() != null ? a.getRating() : 0.0;
                            Double ratingB = b.getRating() != null ? b.getRating() : 0.0;
                            return ratingB.compareTo(ratingA);
                        });
                        break;
                    case "name_asc":
                        slotResults.sort((a, b) -> {
                            String nameA = a.getCourtName() != null ? a.getCourtName() : "";
                            String nameB = b.getCourtName() != null ? b.getCourtName() : "";
                            return nameA.compareToIgnoreCase(nameB);
                        });
                        break;
                    default:
                        // Sort by time first, then by court name
                        slotResults.sort((a, b) -> {
                            int timeCompare = a.getSlotStartTime().compareTo(b.getSlotStartTime());
                            if (timeCompare != 0) return timeCompare;
                            String nameA = a.getCourtName() != null ? a.getCourtName() : "";
                            String nameB = b.getCourtName() != null ? b.getCourtName() : "";
                            return nameA.compareToIgnoreCase(nameB);
                        });
                        break;
                }
                
                // Pagination
                int total = slotResults.size();
                int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
                int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
                int totalPages = (int) Math.ceil((double) total / pageSize);
                int start = (page - 1) * pageSize;
                
                List<com.pickleball.app.dto.court.CourtSlotSearchResult> paginatedResults = slotResults.stream()
                        .skip(start)
                        .limit(pageSize)
                        .collect(Collectors.toList());
                
                // Convert to CourtSearchResponse for backward compatibility
                // Map CourtSlotSearchResult to CourtDTO (bao gồm slot info trong description hoặc custom field)
                // Note: Frontend sẽ cần parse thông tin slot từ response
                // Tạm thời, ta sẽ thêm slot info vào description hoặc tạo một custom field
                List<CourtDTO> courtDTOs = paginatedResults.stream()
                        .map(result -> {
                            CourtDTO dto = new CourtDTO();
                            dto.setCourtId(result.getCourtId());
                            dto.setCourtName(result.getCourtName());
                            dto.setCourtGroupId(result.getCourtGroupId());
                            dto.setCourtGroupName(result.getCourtGroupName());
                            dto.setAddress(result.getAddress());
                            dto.setDistrict(result.getDistrict());
                            dto.setCity(result.getCity());
                            dto.setDescription(result.getDescription());
                            dto.setImages(result.getImages());
                            dto.setPhone(result.getPhone());
                            dto.setAmenities(result.getAmenities());
                            dto.setRating(result.getRating());
                            dto.setReviewCount(result.getReviewCount());
                            // Set price to slot price
                            dto.setBasePricePerHour(result.getSlotPrice());
                            // Store slot info in description as JSON (temporary solution)
                            // Frontend sẽ parse để lấy slot info
                            // Format: "SLOT_INFO:{\"slotId\":123,\"slotDate\":\"2024-01-01\",\"slotStartTime\":\"06:00\",\"slotEndTime\":\"07:00\",\"isLocked\":false}"
                            String slotInfoJson = String.format(
                                "SLOT_INFO:{\"slotId\":%d,\"slotDate\":\"%s\",\"slotStartTime\":\"%s\",\"slotEndTime\":\"%s\",\"isLocked\":%s,\"lockedByUserId\":%s}",
                                result.getSlotId(),
                                result.getSlotDate(),
                                result.getSlotStartTime(),
                                result.getSlotEndTime(),
                                result.getIsLocked(),
                                result.getLockedByUserId() != null ? result.getLockedByUserId() : "null"
                            );
                            // Append to description (temporary)
                            String originalDesc = dto.getDescription() != null ? dto.getDescription() : "";
                            dto.setDescription(originalDesc + "\n" + slotInfoJson);
                            return dto;
                        })
                        .collect(Collectors.toList());
                
                return com.pickleball.app.dto.court.CourtSearchResponse.builder()
                        .courts(courtDTOs)
                        .total((long) total)
                        .page(page)
                        .pageSize(pageSize)
                        .totalPages(totalPages)
                        .build();
            }
        }
        
        // Nếu không có date/time filter, trả về courts như cũ
        // Map to DTOs for price/rating/amenities filtering
        List<CourtDTO> courtDTOs = allCourts.stream()
                .map(this::mapToCourtDTO)
                .collect(Collectors.toList());
        
        // Apply price filters
        if (request.getMinPrice() != null) {
            courtDTOs = courtDTOs.stream()
                    .filter(c -> c.getBasePricePerHour() != null 
                            && c.getBasePricePerHour().compareTo(request.getMinPrice()) >= 0)
                    .collect(Collectors.toList());
        }
        if (request.getMaxPrice() != null) {
            courtDTOs = courtDTOs.stream()
                    .filter(c -> c.getBasePricePerHour() != null 
                            && c.getBasePricePerHour().compareTo(request.getMaxPrice()) <= 0)
                    .collect(Collectors.toList());
        }
        
        // Apply rating filter
        if (request.getMinRating() != null && request.getMinRating() > 0) {
            courtDTOs = courtDTOs.stream()
                    .filter(c -> c.getRating() != null && c.getRating() >= request.getMinRating())
                    .collect(Collectors.toList());
        }
        
        // Apply amenities filter
        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            List<String> requiredAmenities = request.getAmenities();
            courtDTOs = courtDTOs.stream()
                    .filter(c -> {
                        if (c.getAmenities() == null || c.getAmenities().isEmpty()) {
                            return false;
                        }
                        return c.getAmenities().containsAll(requiredAmenities);
                    })
                    .collect(Collectors.toList());
        }
        
        // Apply sorting
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "default";
        switch (sortBy) {
            case "price_asc":
                courtDTOs.sort((a, b) -> {
                    BigDecimal priceA = a.getBasePricePerHour() != null ? a.getBasePricePerHour() : BigDecimal.ZERO;
                    BigDecimal priceB = b.getBasePricePerHour() != null ? b.getBasePricePerHour() : BigDecimal.ZERO;
                    return priceA.compareTo(priceB);
                });
                break;
            case "price_desc":
                courtDTOs.sort((a, b) -> {
                    BigDecimal priceA = a.getBasePricePerHour() != null ? a.getBasePricePerHour() : BigDecimal.ZERO;
                    BigDecimal priceB = b.getBasePricePerHour() != null ? b.getBasePricePerHour() : BigDecimal.ZERO;
                    return priceB.compareTo(priceA);
                });
                break;
            case "rating_desc":
                courtDTOs.sort((a, b) -> {
                    Double ratingA = a.getRating() != null ? a.getRating() : 0.0;
                    Double ratingB = b.getRating() != null ? b.getRating() : 0.0;
                    return ratingB.compareTo(ratingA);
                });
                break;
            case "name_asc":
                courtDTOs.sort((a, b) -> {
                    String nameA = a.getCourtName() != null ? a.getCourtName() : "";
                    String nameB = b.getCourtName() != null ? b.getCourtName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                });
                break;
            default:
                // Keep original order
                break;
        }
        
        // Pagination
        int total = courtDTOs.size();
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int start = (page - 1) * pageSize;
        
        List<CourtDTO> paginatedCourts = courtDTOs.stream()
                .skip(start)
                .limit(pageSize)
                .collect(Collectors.toList());
        
        return com.pickleball.app.dto.court.CourtSearchResponse.builder()
                .courts(paginatedCourts)
                .total((long) total)
                .page(page)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public List<CourtDTO> searchCourts(String date, String startTime, String endTime) {
        // Get all active courts
        List<Court> allCourts = courtRepository.findAll().stream()
                .filter(c -> c.getStatus() == com.pickleball.app.enums.CourtStatus.AVAILABLE)
                .collect(Collectors.toList());
        
        // If date and time provided, filter by availability from TimeSlot
        if (date != null && startTime != null && endTime != null) {
            LocalDate searchDate = LocalDate.parse(date);
            // Normalize time format: "5:00" -> "05:00" để parse được
            String normalizedStartTime = normalizeTimeFormat(startTime);
            String normalizedEndTime = normalizeTimeFormat(endTime);
            LocalTime searchStart = LocalTime.parse(normalizedStartTime);
            LocalTime searchEnd = LocalTime.parse(normalizedEndTime);
            
            // Validate time range
            if (searchStart.isAfter(searchEnd) || searchStart.equals(searchEnd)) {
                return java.util.Collections.emptyList();
            }
            
            // Filter courts that have ALL required time slots available
            return allCourts.stream()
                    .filter(court -> {
                        // Tìm tất cả slots overlapping với khoảng thời gian yêu cầu
                        List<com.pickleball.app.entity.TimeSlot> overlappingSlots = 
                            timeSlotRepository.findOverlappingSlots(
                                court.getCourtId(), 
                                searchDate, 
                                searchStart, 
                                searchEnd
                            );
                        
                        // Kiểm tra: Tất cả slots trong khoảng thời gian phải available
                        // Và phải có đủ slots để cover toàn bộ khoảng thời gian
                        if (overlappingSlots.isEmpty()) {
                            return false; // Không có slots nào cho khoảng thời gian này
                        }
                        
                        // Check tất cả slots phải available và không bị booked
                        boolean allAvailable = overlappingSlots.stream()
                            .allMatch(slot -> slot.getIsAvailable() && slot.getBooking() == null);
                        
                        if (!allAvailable) {
                            return false; // Có slot đã bị booked
                        }
                        
                        // Check slots phải liên tiếp và cover toàn bộ khoảng thời gian
                        // Sắp xếp slots theo startTime
                        overlappingSlots.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));
                        
                        // Check slot đầu tiên phải bắt đầu trước hoặc bằng searchStart
                        LocalTime firstSlotStart = overlappingSlots.get(0).getStartTime();
                        if (firstSlotStart.isAfter(searchStart)) {
                            return false; // Slot đầu tiên không cover searchStart
                        }
                        
                        // Check slot cuối cùng phải kết thúc sau hoặc bằng searchEnd
                        LocalTime lastSlotEnd = overlappingSlots.get(overlappingSlots.size() - 1).getEndTime();
                        if (lastSlotEnd.isBefore(searchEnd)) {
                            return false; // Slot cuối cùng không cover searchEnd
                        }
                        
                        // Check slots phải liên tiếp (không có gap)
                        for (int i = 0; i < overlappingSlots.size() - 1; i++) {
                            LocalTime currentEnd = overlappingSlots.get(i).getEndTime();
                            LocalTime nextStart = overlappingSlots.get(i + 1).getStartTime();
                            if (!currentEnd.equals(nextStart)) {
                                return false; // Có gap giữa các slots
                            }
                        }
                        
                        return true; // Tất cả điều kiện đều thỏa mãn
                    })
                    .map(this::mapToCourtDTO)
                    .collect(Collectors.toList());
        }
        
        // If no date/time filter, return all active courts
        return allCourts.stream()
                .map(this::mapToCourtDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCourtTimeSlots(Long courtId, String date) {
        // Mock generation of slots
        // In real app: 5:00 -> 22:00, exclude booked slots
        List<String> slots = new java.util.ArrayList<>();
        int startHour = 5;
        int endHour = 22;
        for (int i = startHour; i < endHour; i++) {
            slots.add(String.format("%02d:00", i));
        }
        return slots;
    }

    @Override
    public com.pickleball.app.dto.court.CourtDetailDTO getCourtDetailById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        // Load image IDs for court and court group
        List<Long> courtImageIds = courtImageRepository
                .findByCourtOrderBySortOrderAscImageIdAsc(court).stream()
                .map(CourtImage::getImageId)
                .collect(Collectors.toList());

        List<Long> courtGroupImageIds = courtGroupImageRepository
                .findByCourtGroupOrderBySortOrderAscImageIdAsc(court.getCourtGroup()).stream()
                .map(CourtGroupImage::getImageId)
                .collect(Collectors.toList());

        return com.pickleball.app.dto.court.CourtDetailDTO.builder()
                .courtId(court.getCourtId())
                .courtGroupId(court.getCourtGroup().getCourtGroupId())
                .courtName(court.getCourtName())
                .status(court.getStatus())
                .basePricePerHour(court.getBasePricePerHour())
                .courtGroupName(court.getCourtGroup().getGroupName())
                .address(court.getCourtGroup().getAddress())
                .district(court.getCourtGroup().getDistrict())
                .city(court.getCourtGroup().getCity())
                .description(court.getCourtGroup().getDescription())
                .images(court.getCourtGroup().getImages())
                .courtImageIds(courtImageIds)
                .courtGroupImageIds(courtGroupImageIds)
                .build();
    }

    @Override
    public List<TimeSlotDTO> getAvailableTimeSlots(Long courtId, String date) {
        LocalDate bookingDate = LocalDate.parse(date);
        
        // Lấy time slots từ database
        List<TimeSlotDTO> slots = timeSlotService.getAvailableTimeSlotsForDate(courtId, bookingDate);
        
        // Nếu chưa có time slots trong database, tạo mới (sẽ tự động lấy từ config)
        if (slots.isEmpty()) {
            // Gọi với tham số 0,0,0 để tự động lấy từ config
            timeSlotService.generateTimeSlotsForDate(courtId, bookingDate, 0, 0, 0);
            slots = timeSlotService.getAvailableTimeSlotsForDate(courtId, bookingDate);
        }
        
        return slots;
    }

    @Override
    public boolean checkTimeSlotAvailability(Long courtId, String date, String startTime, String endTime) {
        LocalDate bookingDate = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        // Check từ TimeSlot table (đúng nghiệp vụ)
        List<com.pickleball.app.entity.TimeSlot> overlappingSlots = 
            timeSlotRepository.findOverlappingSlots(courtId, bookingDate, start, end);
        
        // Tất cả slots phải available
        return !overlappingSlots.isEmpty() && 
               overlappingSlots.stream()
                   .allMatch(slot -> slot.getIsAvailable() && slot.getBooking() == null);
    }

    // --- Mappers ---

    private CourtGroupDTO mapToCourtGroupDTO(CourtGroup entity) {
        CourtGroupDTO dto = new CourtGroupDTO();
        dto.setCourtGroupId(entity.getCourtGroupId());
        dto.setGroupName(entity.getGroupName());
        dto.setAddress(entity.getAddress());
        dto.setDistrict(entity.getDistrict());
        dto.setCity(entity.getCity());
        dto.setDescription(entity.getDescription());
        dto.setImages(entity.getImages());
        // Map image IDs from court_group_images table
        List<Long> imageIds = courtGroupImageRepository
                .findByCourtGroupOrderBySortOrderAscImageIdAsc(entity).stream()
                .map(CourtGroupImage::getImageId)
                .collect(Collectors.toList());
        dto.setImageIds(imageIds);
        if (entity.getManager() != null) {
            dto.setManagerId(entity.getManager().getUserId());
        }
        return dto;
    }

    private CourtDTO mapToCourtDTO(Court entity) {
        CourtDTO dto = new CourtDTO();
        CourtGroup courtGroup = entity.getCourtGroup();
        
        // Basic court fields
        dto.setCourtId(entity.getCourtId());
        dto.setCourtGroupId(courtGroup.getCourtGroupId());
        dto.setCourtName(entity.getCourtName());
        dto.setStatus(entity.getStatus());
        dto.setBasePricePerHour(entity.getBasePricePerHour());
        
        // Fields from CourtGroup
        dto.setDistrict(courtGroup.getDistrict());
        dto.setCity(courtGroup.getCity());
        dto.setCourtGroupName(courtGroup.getGroupName());
        dto.setAddress(courtGroup.getAddress()); // Location/address
        dto.setDescription(courtGroup.getDescription());
        dto.setImages(courtGroup.getImages()); // Images (JSON string or comma-separated)

        // Image IDs
        List<Long> courtImageIds = courtImageRepository
                .findByCourtOrderBySortOrderAscImageIdAsc(entity).stream()
                .map(CourtImage::getImageId)
                .collect(Collectors.toList());
        dto.setCourtImageIds(courtImageIds);

        List<Long> courtGroupImageIds = courtGroupImageRepository
                .findByCourtGroupOrderBySortOrderAscImageIdAsc(courtGroup).stream()
                .map(CourtGroupImage::getImageId)
                .collect(Collectors.toList());
        dto.setCourtGroupImageIds(courtGroupImageIds);
        
        // Phone from manager if available
        if (courtGroup.getManager() != null && courtGroup.getManager().getPhoneNumber() != null) {
            dto.setPhone(courtGroup.getManager().getPhoneNumber());
        }
        
        // Amenities from Service entities (active services only)
        List<String> amenities = serviceRepository.findByCourtGroup(courtGroup).stream()
                .filter(s -> s.getStatus() == ServiceStatus.AVAILABLE)
                .map(com.pickleball.app.entity.Service::getServiceName)
                .collect(Collectors.toList());
        dto.setAmenities(amenities);
        
        // Rating and review count (can be calculated from reviews if available)
        // For now, set default values or calculate from reviews if Review entity exists
        dto.setRating(null); // TODO: Calculate from reviews if Review entity exists
        dto.setReviewCount(0); // TODO: Count reviews if Review entity exists
        
        return dto;
    }

    private com.pickleball.app.dto.court.CourtSlotSearchResult mapToCourtSlotSearchResult(
            Court court, com.pickleball.app.entity.TimeSlot slot) {
        CourtGroup courtGroup = court.getCourtGroup();
        
        com.pickleball.app.dto.court.CourtSlotSearchResult result = 
            com.pickleball.app.dto.court.CourtSlotSearchResult.builder()
                // Court info
                .courtId(court.getCourtId())
                .courtName(court.getCourtName())
                .courtGroupId(courtGroup.getCourtGroupId())
                .courtGroupName(courtGroup.getGroupName())
                .address(courtGroup.getAddress())
                .district(courtGroup.getDistrict())
                .city(courtGroup.getCity())
                .description(courtGroup.getDescription())
                .images(courtGroup.getImages())
                .phone(courtGroup.getManager() != null && courtGroup.getManager().getPhoneNumber() != null 
                        ? courtGroup.getManager().getPhoneNumber() : null)
                // Amenities
                .amenities(serviceRepository.findByCourtGroup(courtGroup).stream()
                        .filter(s -> s.getStatus() == ServiceStatus.AVAILABLE)
                        .map(com.pickleball.app.entity.Service::getServiceName)
                        .collect(Collectors.toList()))
                .rating(null) // TODO: Calculate from reviews
                .reviewCount(0) // TODO: Count reviews
                // Slot info
                .slotId(slot.getSlotId())
                .slotDate(slot.getSlotDate().toString())
                .slotStartTime(slot.getStartTime().toString())
                .slotEndTime(slot.getEndTime().toString())
                .slotPrice(slot.getPrice())
                .isAvailable(slot.getIsAvailable())
                .isLocked(false) // Will be set later
                .lockedByUserId(null) // Will be set later
                .build();
        
        return result;
    }

    @Override
    public List<String> getDistricts() {
        return countGroupRepository.findAll().stream()
                .map(CourtGroup::getDistrict)
                .filter(district -> district != null && !district.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCities() {
        return countGroupRepository.findAll().stream()
                .map(CourtGroup::getCity)
                .filter(city -> city != null && !city.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // --- Images ---

    @Override
    public List<Long> uploadCourtGroupImage(Long courtGroupId, MultipartFile file) {
        CourtGroup group = countGroupRepository.findById(courtGroupId)
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        try {
            // Determine next sort order
            List<CourtGroupImage> existing = courtGroupImageRepository
                    .findByCourtGroupOrderBySortOrderAscImageIdAsc(group);
            int nextSortOrder = existing.isEmpty()
                    ? 0
                    : (existing.get(existing.size() - 1).getSortOrder() != null
                            ? existing.get(existing.size() - 1).getSortOrder() + 1
                            : existing.size());

            CourtGroupImage image = CourtGroupImage.builder()
                    .courtGroup(group)
                    .imageData(file.getBytes())
                    .contentType(file.getContentType())
                    .fileName(file.getOriginalFilename())
                    .sortOrder(nextSortOrder)
                    .build();

            courtGroupImageRepository.save(image);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read image data", e);
        }

        return courtGroupImageRepository
                .findByCourtGroupOrderBySortOrderAscImageIdAsc(group).stream()
                .map(CourtGroupImage::getImageId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> uploadCourtImage(Long courtId, MultipartFile file) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        try {
            List<CourtImage> existing = courtImageRepository
                    .findByCourtOrderBySortOrderAscImageIdAsc(court);
            int nextSortOrder = existing.isEmpty()
                    ? 0
                    : (existing.get(existing.size() - 1).getSortOrder() != null
                            ? existing.get(existing.size() - 1).getSortOrder() + 1
                            : existing.size());

            CourtImage image = CourtImage.builder()
                    .court(court)
                    .imageData(file.getBytes())
                    .contentType(file.getContentType())
                    .fileName(file.getOriginalFilename())
                    .sortOrder(nextSortOrder)
                    .build();

            courtImageRepository.save(image);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read image data", e);
        }

        return courtImageRepository
                .findByCourtOrderBySortOrderAscImageIdAsc(court).stream()
                .map(CourtImage::getImageId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImageDTO> getCourtGroupImages(Long courtGroupId) {
        CourtGroup group = countGroupRepository.findById(courtGroupId)
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        return courtGroupImageRepository
                .findByCourtGroupOrderBySortOrderAscImageIdAsc(group).stream()
                .map(this::mapToImageDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ImageDTO> getCourtImages(Long courtId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new RuntimeException("Court not found"));

        return courtImageRepository
                .findByCourtOrderBySortOrderAscImageIdAsc(court).stream()
                .map(this::mapToImageDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ImageDTO getCourtGroupImageById(Long imageId) {
        CourtGroupImage image = courtGroupImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Court Group image not found"));
        return mapToImageDTO(image);
    }

    @Override
    public ImageDTO getCourtImageById(Long imageId) {
        CourtImage image = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Court image not found"));
        return mapToImageDTO(image);
    }

    private ImageDTO mapToImageDTO(CourtGroupImage image) {
        String base64 = java.util.Base64.getEncoder().encodeToString(image.getImageData());
        return ImageDTO.builder()
                .imageId(image.getImageId())
                .fileName(image.getFileName())
                .contentType(image.getContentType())
                .data(base64)
                .build();
    }

    private ImageDTO mapToImageDTO(CourtImage image) {
        String base64 = java.util.Base64.getEncoder().encodeToString(image.getImageData());
        return ImageDTO.builder()
                .imageId(image.getImageId())
                .fileName(image.getFileName())
                .contentType(image.getContentType())
                .data(base64)
                .build();
    }

    /**
     * Normalize time format từ "H:mm" sang "HH:mm" để LocalTime.parse() có thể parse được
     * Ví dụ: "5:00" -> "05:00", "9:30" -> "09:30"
     */
    private String normalizeTimeFormat(String time) {
        if (time == null || time.trim().isEmpty()) {
            return time;
        }
        String trimmed = time.trim();
        // Nếu format là "H:mm" (1 chữ số), chuyển thành "HH:mm" (2 chữ số)
        if (trimmed.matches("^\\d{1}:\\d{2}$")) {
            return String.format("%02d:%s", Integer.parseInt(trimmed.split(":")[0]), trimmed.split(":")[1]);
        }
        // Nếu đã là "HH:mm" hoặc format khác, trả về như cũ
        return trimmed;
    }
}
