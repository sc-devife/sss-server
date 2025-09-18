package com.sss.app.helper;

import com.sss.app.entity.system.Currency;
import com.sss.app.exception.NotFoundException;
import com.sss.app.repository.system.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SystemHelper {

    private final CurrencyRepository currencyRepository;

    public List<Currency> getSupportedCurrencies() {
        List<Currency> currencies = currencyRepository.findAll();
        if (currencies.isEmpty()) {
            throw new NotFoundException("No supported currencies found.");
        }
        return currencies;
    }
}
