package com.sss.app.repository.exchangerate;

import com.sss.app.entity.exchangerate.OrgExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgExchangeRateRepository extends JpaRepository<OrgExchangeRate, Long> {

    Optional<OrgExchangeRate> findByOrgIdAndFromCurrencyAndToCurrency(Long orgId, String fromCurrency, String toCurrency);

    List<OrgExchangeRate> findAllByOrgIdAndFromCurrency(Long orgId, String fromCurrency);
}
