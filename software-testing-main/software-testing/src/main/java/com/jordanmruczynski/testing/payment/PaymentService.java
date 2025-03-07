package com.jordanmruczynski.testing.payment;

import com.jordanmruczynski.testing.customer.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final List<Currency> ACCEPTED_CURRENCIES = List.of(Currency.PLN, Currency.GBP);

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;

    @Autowired
    public PaymentService(CustomerRepository customerRepository, PaymentRepository paymentRepository, CardPaymentCharger cardPaymentCharger) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.cardPaymentCharger = cardPaymentCharger;
    }

    void chargeCard(UUID customerId, PaymentRequest paymentRequest) {
        customerRepository.findById(customerId).ifPresentOrElse(
                (customer) -> {
                    Payment payment = paymentRequest.getPayment();

                    if (!ACCEPTED_CURRENCIES.stream().anyMatch(c -> c.equals(payment.getCurrency()))) {
                        throw new IllegalStateException("This currency is not supported");
                    }

                    CardPaymentCharge cardPaymentCharge = cardPaymentCharger.chargeCard(payment.getSource(), payment.getAmount(), payment.getCurrency(), payment.getDescription());

                    if (!cardPaymentCharge.isCardDebited()) {
                        throw new IllegalStateException(String.format("Card not debited for customer id: %s", customerId));
                    }

                    payment.setCustomerId(customerId);
                    paymentRepository.save(payment);
                },

                () -> {
                    throw new IllegalStateException(String.format("Customer with id: [%s] not found", customerId));
                }
        );
    }
}
