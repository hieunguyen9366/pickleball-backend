package com.pickleball.app.service;

import com.pickleball.app.dto.service.ServiceDTO;

import java.util.List;

public interface ServiceService {
    List<ServiceDTO> getServicesByCourtGroup(Long courtGroupId);

    List<ServiceDTO> searchServices(Long courtGroupId, String status, String keyword);

    ServiceDTO createService(ServiceDTO dto);

    ServiceDTO updateService(Long id, ServiceDTO dto);

    void deleteService(Long id);
    
    ServiceDTO getServiceById(Long id);
}
