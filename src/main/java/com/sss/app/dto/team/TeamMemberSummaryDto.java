package com.sss.app.dto.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberSummaryDto {
    private Long seqp;
    private String uid;
    private String name;
    private String email;
}
