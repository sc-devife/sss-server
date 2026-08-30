package com.sss.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/** One bucket of the Leads Trend chart — "date" is "yyyy-MM-dd" (day granularity) or "yyyy-MM" (month granularity). */
@Data
@AllArgsConstructor
public class LeadsTrendPointDTO {
    private String date;
    private long count;
}
