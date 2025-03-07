package com.example.examplerest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final RestTemplate restTemplate;

    @Value("${cb.url}")
    private String cbUrl;

    @Cacheable("currencies")
    public HashMap<String, String> getCurrencies() {
        ResponseEntity<HashMap> currencies = restTemplate.getForEntity(cbUrl + "?currency=USD", HashMap.class);
        HashMap<String, String> currencyMap = currencies.getBody();
        return currencyMap;
    }

}
