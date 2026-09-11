package com.sss.app.dto.notification;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponseDTO {
    private UUID uid;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private String relatedEntityType;
    private UUID relatedEntityUid;
}
