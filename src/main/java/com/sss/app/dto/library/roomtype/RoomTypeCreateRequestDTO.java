package com.sss.app.dto.library.roomtype;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoomTypeCreateRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
