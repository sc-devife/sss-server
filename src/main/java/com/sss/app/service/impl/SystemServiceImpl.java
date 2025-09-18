package com.sss.app.service.impl;

import com.sss.app.dto.system.CurrencyResponseDto;
import com.sss.app.helper.SystemHelper;
import com.sss.app.mapper.SystemMapper;
import com.sss.app.service.SystemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemServiceImpl implements SystemService {

    SystemHelper systemHelper;
    SystemMapper systemMapper;

    public SystemServiceImpl(SystemHelper systemHelper, SystemMapper systemMapper) {
        this.systemHelper = systemHelper;
        this.systemMapper = systemMapper;
    }

    @Override
    public List<CurrencyResponseDto> getSupportedCurrencies() {
        return systemMapper.toCurrencyResponseDtoList(systemHelper.getSupportedCurrencies());
    }
}
