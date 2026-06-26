package com.sss.app.dto.library.roomtype;

import lombok.Data;

import java.util.UUID;

@Data
public class RoomTypeResponseDTO {

    private UUID uid;

    private String name;

    private String description;

    private Boolean isActive;
}
