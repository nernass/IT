package com.jordanmruczynski.testing.payment.stripe;

import com.jordanmruczynski.testing.payment.CardPaymentCharge;
import com.jordanmruczynski.testing.payment.CardPaymentCharger;
import com.jordanmruczynski.testing.payment.Currency;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(
        value = "stripe.enabled",
        havingValue = "false"
)
public class MockStripeService implements CardPaymentCharger {

    @Override
    public CardPaymentCharge chargeCard(String source, BigDecimal amount, Currency currency, String description) {
        return new CardPaymentCharge(true);
    }
}
