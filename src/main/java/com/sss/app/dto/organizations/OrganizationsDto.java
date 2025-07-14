package com.sss.app.dto.organizations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationsDto {
    private Long seqp;
    private String registered_name;
    private String display_name;
    private String support_ph_num;
}
