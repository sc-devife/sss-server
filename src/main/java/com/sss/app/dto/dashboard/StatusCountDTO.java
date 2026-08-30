package com.sss.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;

/** One status bucket in a breakdown (Lead Funnel, Escape Pipeline, Quote status). */
@Data
@AllArgsConstructor
public class StatusCountDTO {
    private String status;
    private long count;
}
