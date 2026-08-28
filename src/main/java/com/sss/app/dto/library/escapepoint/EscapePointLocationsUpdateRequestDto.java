package com.sss.app.dto.library.escapepoint;

import lombok.Data;

import java.util.List;

@Data
public class EscapePointLocationsUpdateRequestDto {
    private List<String> locationUids;
    // Must be one of locationUids, or null (no headline city set yet).
    private String primaryLocationUid;
}
