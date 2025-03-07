package com.jordanmruczynski.testing.payment;

import com.jordanmruczynski.testing.customer.Customer;
import com.jordanmruczynski.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private PaymentService underTest;

    @Mock
    private PaymentRepository paymentRepository = mock(PaymentRepository.class);
    @Mock
    private CardPaymentCharger cardPaymentCharger = mock(CardPaymentCharger.class);
    @Mock
    private CustomerRepository customerRepository = mock(CustomerRepository.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(customerRepository, paymentRepository, cardPaymentCharger);
    }

    @Test
    void itShouldThrowExceptionWhenCustomerDoesNotExists() {
        // Given
        long paymentId = 1L;
        UUID customerId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, customerId, new BigDecimal("10.00"), Currency.PLN, "card123", "Donation");
        // Request
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        // Non existing customer is returned
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());
        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Customer with id: [%s] not found", customerId));
        // Finally
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();

    }

    @Test
    void itShouldChargeCardSuccessfully() {
        // Given
        long paymentId = 1L;
        UUID customerId = UUID.randomUUID();
        Currency currency = Currency.PLN;
        Payment payment = new Payment(paymentId, customerId, new BigDecimal("10.00"), currency, "card123", "Donation");

        // Request
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // Existing customer is returned
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // Card is charged successfully
        given(cardPaymentCharger.chargeCard(payment.getSource(), payment.getAmount(), payment.getCurrency(), payment.getDescription())).willReturn(new CardPaymentCharge(true));
        underTest.chargeCard(customerId, paymentRequest);

        // Then
        ArgumentCaptor<Payment> paymentArgumentCaptor = ArgumentCaptor.forClass(Payment.class);
        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();
        assertThat(paymentArgumentCaptorValue).isEqualToComparingFieldByField(paymentRequest.getPayment());

        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);

    }

    @Test
    void itShouldThrowExceptionWhenCardIsNotDebited() {
        // Given
        long paymentId = 1L;
        UUID customerId = UUID.randomUUID();
        Currency currency = Currency.PLN;
        Payment payment = new Payment(paymentId, customerId, new BigDecimal("10.00"), currency, "card123", "Donation");

        // Request
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // Existing customer is returned
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // Card is charged unsuccessfully
        given(cardPaymentCharger.chargeCard(payment.getSource(), payment.getAmount(), payment.getCurrency(), payment.getDescription())).willReturn(new CardPaymentCharge(false));

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(String.format("Card not debited for customer id: %s", customerId));

        then(paymentRepository).should(never()).save(any());

    }

    @Test
    void itShouldThrowExceptionWhenCurrencyIsNotSupported() {
        // Given
        long paymentId = 1L;
        UUID customerId = UUID.randomUUID();
        Currency currency = Currency.USD;
        Payment payment = new Payment(paymentId, customerId, new BigDecimal("10.00"), currency, "card123", "Donation");

        // Request
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // Existing customer is returned
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // When
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This currency is not supported");

        // Then
        // No interaction with cardPaymentCharger
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();

    }

}