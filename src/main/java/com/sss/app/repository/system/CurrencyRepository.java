package com.sss.app.repository.system;

import com.sss.app.entity.system.Currency;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CurrencyRepository extends JpaRepository<Currency, Long> {
}
