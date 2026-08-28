package com.sss.app.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionResponseDto {
    private UUID sessionId;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;

    // Lombok's boolean getter for a field already prefixed "is" is
    // isCurrent() (not isIsCurrent()) — Jackson then strips that "is" itself
    // when deriving the JSON property name, silently emitting "current"
    // instead of "isCurrent" unless pinned explicitly here.
    @JsonProperty("isCurrent")
    private boolean isCurrent;
}
