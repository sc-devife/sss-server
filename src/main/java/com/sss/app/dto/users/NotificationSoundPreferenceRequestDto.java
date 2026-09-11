package com.sss.app.dto.users;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationSoundPreferenceRequestDto {
    @NotNull(message = "enabled is required")
    private Boolean enabled;
}
