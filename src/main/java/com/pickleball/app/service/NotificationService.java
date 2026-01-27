package com.pickleball.app.service;

import com.pickleball.app.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification createNotification(Long userId, String title, String message, String type);

    List<Notification> getUserNotifications(Long userId);

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);
}
