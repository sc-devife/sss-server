package com.sss.app.dto.users.invitations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInvitationDto {
    private Long seqp;
    private String uid;
    private String email;
    private LocalDateTime expires_set;
}
