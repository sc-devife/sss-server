package com.sss.app.service;

import com.sss.app.dto.system.CurrencyResponseDto;

import java.util.List;

public interface SystemService {
    List<CurrencyResponseDto> getSupportedCurrencies();
}
