package com.sss.app.dto.library.roomtype;

import lombok.Data;

@Data
public class RoomTypeUpdateRequestDTO {

    private String name;

    private String description;

    private Boolean isActive;
}
