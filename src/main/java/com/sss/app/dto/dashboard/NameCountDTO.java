package com.sss.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/** One named bucket in a breakdown (Lead Source, Top Escape Points). */
@Data
@AllArgsConstructor
public class NameCountDTO {
    private String name;
    private long count;
}
