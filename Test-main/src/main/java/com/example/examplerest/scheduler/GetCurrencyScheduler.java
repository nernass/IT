package com.example.examplerest.scheduler;

import com.example.examplerest.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCurrencyScheduler {

    private final CurrencyService currencyService;

    @Scheduled(cron = "0 * * * * *")
    public void getCurrencyFromCB() {
        currencyService.getCurrencies();
    }

}
