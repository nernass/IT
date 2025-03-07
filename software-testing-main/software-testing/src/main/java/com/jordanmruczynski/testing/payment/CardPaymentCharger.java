package com.jordanmruczynski.testing.payment;

import java.math.BigDecimal;

public interface CardPaymentCharger {

    CardPaymentCharge chargeCard(
            String source,
            BigDecimal amount,
            Currency currency,
            String description
    );
}
