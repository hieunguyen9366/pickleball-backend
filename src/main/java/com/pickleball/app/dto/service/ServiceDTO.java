package com.pickleball.app.dto.service;

import com.pickleball.app.enums.ServiceStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServiceDTO {
    private Long serviceId;
    private Long courtGroupId;
    private String serviceName;
    private String unit;
    private BigDecimal price;
    private String imageUrl;
    private ServiceStatus status;
}
