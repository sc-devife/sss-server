package com.sss.app.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * "My Escapes" dashboard card — deliberately NOT the full EscapeResponseDTO,
 * whose escapePoints[].images carries every image for every linked
 * destination. Only the one resolved cover image is included here, so this
 * card's payload stays small regardless of how many images an escape's
 * destinations have.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEscapeSummaryDTO {
    private UUID uid;
    private String leadName;
    private List<String> escapePointNames;
    private String status;
    /** First image of the escape's first escape point, or null if it has none. */
    private String imageUrl;
}
