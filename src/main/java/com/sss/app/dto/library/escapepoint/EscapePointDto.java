package com.sss.app.dto.library.escapepoint;

import lombok.Data;

import java.util.List;

@Data
public class EscapePointDto {
    private String id;
    private String name;
    private String nick_name;
    private String nearest_airport;
    private String currency;
    private String time_zone;
    private String tags;
    private String remarks;
    private String description;
    private List<String> images;
    private String status;
}
