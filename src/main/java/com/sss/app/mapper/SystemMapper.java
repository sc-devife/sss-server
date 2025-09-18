package com.sss.app.mapper;

import com.sss.app.dto.system.CurrencyResponseDto;
import com.sss.app.entity.system.Currency;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SystemMapper {
    public CurrencyResponseDto toCurrencyResponseDto(Currency currency) {
        CurrencyResponseDto dto = new CurrencyResponseDto();
        dto.setSeqp(currency.getSeqp());
        dto.setUid(currency.getUid());
        dto.setCode(currency.getCode());
        dto.setName(currency.getName());
        dto.setSymbol(currency.getSymbol());
        dto.setIs_active(currency.getIs_active());

        return dto;
    }

    public List<CurrencyResponseDto> toCurrencyResponseDtoList(List<Currency> currencies) {
        return currencies.stream()
                .map(this::toCurrencyResponseDto)
                .collect(Collectors.toList());
    }
}
