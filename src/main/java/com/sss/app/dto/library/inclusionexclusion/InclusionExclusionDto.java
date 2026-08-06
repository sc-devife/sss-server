package com.sss.app.dto.library.inclusionexclusion;

import lombok.Data;

@Data
public class InclusionExclusionDto {
    private String name;
    private String type; // INCLUSION / EXCLUSION / TERMS
    private String contentHtml;
    private String escapePointId; // EscapePoint uid, optional — unlinked items live in the org-wide library
    private Long sortOrder;
}
