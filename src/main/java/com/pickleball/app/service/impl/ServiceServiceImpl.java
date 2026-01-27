package com.pickleball.app.service.impl;

import com.pickleball.app.dto.service.ServiceDTO;
import com.pickleball.app.entity.CourtGroup;
import com.pickleball.app.entity.Service;
import com.pickleball.app.repository.CourtGroupRepository;
import com.pickleball.app.repository.ServiceRepository;
import com.pickleball.app.service.ServiceService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final CourtGroupRepository countGroupRepository;

    @Override
    public List<ServiceDTO> getServicesByCourtGroup(Long courtGroupId) {
        CourtGroup group = countGroupRepository.findById(courtGroupId)
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        return serviceRepository.findByCourtGroup(group).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDTO> searchServices(Long courtGroupId, String status, String keyword) {
        // Simple implementation: fetch all and filter in memory or use proper query
        // For MVP/Demo correctness, in-memory filtering is fine if dataset small
        return serviceRepository.findAll().stream()
                .filter(s -> courtGroupId == null || s.getCourtGroup().getCourtGroupId().equals(courtGroupId))
                .filter(s -> status == null || s.getStatus().name().equals(status))
                .filter(s -> keyword == null || s.getServiceName().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceDTO createService(ServiceDTO dto) {
        CourtGroup group = countGroupRepository.findById(dto.getCourtGroupId())
                .orElseThrow(() -> new RuntimeException("Court Group not found"));

        Service service = new Service();
        service.setCourtGroup(group);
        service.setServiceName(dto.getServiceName());
        service.setUnit(dto.getUnit());
        service.setPrice(dto.getPrice());
        service.setImageUrl(dto.getImageUrl());
        service.setStatus(dto.getStatus());

        Service saved = serviceRepository.save(service);
        return mapToDTO(saved);
    }

    @Override
    public ServiceDTO updateService(Long id, ServiceDTO dto) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        service.setServiceName(dto.getServiceName());
        service.setUnit(dto.getUnit());
        service.setPrice(dto.getPrice());
        service.setImageUrl(dto.getImageUrl());
        service.setStatus(dto.getStatus());

        Service saved = serviceRepository.save(service);
        return mapToDTO(saved);
    }

    @Override
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }

    @Override
    public ServiceDTO getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        return mapToDTO(service);
    }

    private ServiceDTO mapToDTO(Service entity) {
        ServiceDTO dto = new ServiceDTO();
        dto.setServiceId(entity.getServiceId());
        dto.setCourtGroupId(entity.getCourtGroup().getCourtGroupId());
        dto.setServiceName(entity.getServiceName());
        dto.setUnit(entity.getUnit());
        dto.setPrice(entity.getPrice());
        dto.setImageUrl(entity.getImageUrl());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
