package com.sss.app.dto.system;

import lombok.Data;

@Data
public class CurrencyDto {
    private String code;
    private String name;
    private String symbol;
    private Boolean is_active;
}
