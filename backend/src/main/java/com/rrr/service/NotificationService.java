package com.rrr.service;

import com.rrr.dto.NotificationDto;
import com.rrr.exception.ResourceNotFoundException;
import com.rrr.model.Notification;
import com.rrr.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public NotificationDto createAndSendNotification(UUID userId, String title, String message, String type, UUID referenceId) {
        Notification notification = new Notification(userId, title, message, type, referenceId);
        notification = notificationRepository.save(notification);

        NotificationDto dto = mapToDto(notification);

        try {
            messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", dto);
            messagingTemplate.convertAndSend("/topic/sos-alerts", dto);
        } catch (Exception e) {
            // Log but don't fail transactional DB persistence
        }

        return dto;
    }

    public List<NotificationDto> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : notifications) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

    private NotificationDto mapToDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setUserId(n.getUserId());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setReferenceId(n.getReferenceId());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
