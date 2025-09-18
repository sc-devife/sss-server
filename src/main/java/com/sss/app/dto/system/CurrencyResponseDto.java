package com.sss.app.dto.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CurrencyResponseDto extends CurrencyDto {
    private Long seqp;
    private String uid;
}
