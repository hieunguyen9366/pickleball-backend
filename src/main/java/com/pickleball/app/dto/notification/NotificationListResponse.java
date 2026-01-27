package com.pickleball.app.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private List<NotificationDTO> notifications;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
}



