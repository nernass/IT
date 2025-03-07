package com.jordanmruczynski.testing.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordanmruczynski.testing.customer.Customer;
import com.jordanmruczynski.testing.customer.CustomerRegistrationRequest;
import com.jordanmruczynski.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CardPaymentCharger cardPaymentCharger;

    private UUID customerId;

    @BeforeEach
    void setup() {
        // Clear repositories
        paymentRepository.deleteAll();
        customerRepository.deleteAll();

        // Create a test customer
        customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John Doe", "+1234567890");
        customerRepository.save(customer);
    }

    @Test
    void canRegisterCustomerAndMakePayment() throws Exception {
        // Given
        UUID newCustomerId = UUID.randomUUID();
        Customer customer = new Customer(newCustomerId, "Jane Smith", "+0987654321");
        CustomerRegistrationRequest registrationRequest = new CustomerRegistrationRequest(customer);

        // Register new customer
        ResultActions customerRegistrationResult = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        // Then
        customerRegistrationResult.andExpect(status().isOk());
        assertThat(customerRepository.findById(newCustomerId)).isPresent();

        // Given
        Payment payment = new Payment(
                null,
                newCustomerId,
                new BigDecimal("100.00"),
                Currency.GBP,
                "card123",
                "Payment for test");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When making payment
        ResultActions paymentResult = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().isOk());

        // Verify payment was saved
        Iterable<Payment> allPayments = paymentRepository.findAll();
        assertThat(allPayments).hasSize(1);
        Payment savedPayment = allPayments.iterator().next();
        assertThat(savedPayment.getCustomerId()).isEqualTo(newCustomerId);
        assertThat(savedPayment.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldNotProcessPaymentWhenCurrencyNotSupported() throws Exception {
        // Given
        Payment payment = new Payment(
                null,
                customerId,
                new BigDecimal("100.00"),
                Currency.USD, // Using unsupported currency
                "card123",
                "Payment with unsupported currency");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When making payment
        ResultActions paymentResult = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().is5xxServerError());

        // No payments should be saved
        Iterable<Payment> allPayments = paymentRepository.findAll();
        assertThat(allPayments).isEmpty();
    }

    @Test
    void shouldNotProcessPaymentWhenCustomerDoesNotExist() throws Exception {
        // Given
        UUID nonExistentCustomerId = UUID.randomUUID();
        Payment payment = new Payment(
                null,
                nonExistentCustomerId,
                new BigDecimal("100.00"),
                Currency.PLN,
                "card123",
                "Payment for non-existent customer");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When making payment
        ResultActions paymentResult = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().is5xxServerError());

        // No payments should be saved
        Iterable<Payment> allPayments = paymentRepository.findAll();
        assertThat(allPayments).isEmpty();
    }

    @Test
    void cannotRegisterCustomerWithDuplicatePhoneNumber() throws Exception {
        // Given
        UUID newCustomerId = UUID.randomUUID();
        Customer customer = new Customer(newCustomerId, "Duplicate Phone", "+1234567890"); // Same phone as in setup
        CustomerRegistrationRequest registrationRequest = new CustomerRegistrationRequest(customer);

        // When
        ResultActions customerRegistrationResult = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)));

        // Then
        customerRegistrationResult.andExpect(status().is4xxClientError());

        // Only the original customer should exist
        assertThat(customerRepository.count()).isEqualTo(1);
    }
}