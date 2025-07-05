package com.sss.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long seqp;
    private String uid;
    private String name;
    private String first_name;
    private String last_name;
    private String email;
    private String contact_number;
}
