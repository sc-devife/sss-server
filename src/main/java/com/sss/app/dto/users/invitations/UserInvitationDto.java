package com.sss.app.dto.users.invitations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInvitationDto {
        private Long seqp;
        private String uid;
        private String email;
       // private String ExpiryDate;
}
