package com.sss.app.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// Enables @Async — needed so PaymentConfirmationEmailService's SMTP round
// trip (observed to take up to ~25s in this environment) never blocks the
// "Verify Payment" HTTP request. No custom executor bean: this is low-volume
// (one call per verified milestone), so the default SimpleAsyncTaskExecutor
// (a fresh thread per call) is sufficient.
@Configuration
@EnableAsync
public class AsyncConfig {
}
