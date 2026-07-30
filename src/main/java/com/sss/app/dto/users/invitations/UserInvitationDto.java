package com.sss.app.dto.users.invitations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInvitationDto {
    private Long seqp;
    private String uid;
    private String email;
    private LocalDateTime expires_set;
    private List<String> roles;
}
