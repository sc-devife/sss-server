package com.sss.app.dto.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lightweight team reference attached to UserResponseDto — the reverse
// direction of TeamMemberSummaryDto (which attaches users to a team).
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRefDto {
    private Long seqp;
    private String uid;
    private String name;
}
