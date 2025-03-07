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

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void canMakePaymentWhenCustomerExists() throws Exception {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "James Bond", "+447123456789");
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        // Register the customer
        ResultActions customerRegResult = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRegistrationRequest)));

        // Then
        customerRegResult.andExpect(status().isOk());

        // Given
        Payment payment = new Payment(
                null,
                customerId,
                new BigDecimal("100.00"),
                Currency.GBP,
                "card123",
                "Payment for test"
        );
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When
        ResultActions paymentResult = mockMvc.perform(put("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().isOk());

        // Additional Verifications
        assertThat(paymentRepository.findAll())
                .hasSize(1)
                .extracting("customerId")
                .contains(customerId);
    }

    @Test
    void cannotMakePaymentWhenCustomerDoesNotExist() throws Exception {
        // Given
        UUID customerId = UUID.randomUUID();
        Payment payment = new Payment(
                null,
                customerId,
                new BigDecimal("100.00"),
                Currency.GBP,
                "card123",
                "Payment for test"
        );
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When
        ResultActions paymentResult = mockMvc.perform(put("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().is5xxServerError());
        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @Test
    void cannotMakePaymentWithUnsupportedCurrency() throws Exception {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "James Bond", "+447123456789");
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        // Register the customer
        mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRegistrationRequest)))
                .andExpect(status().isOk());

        // Given
        Payment payment = new Payment(
                null,
                customerId,
                new BigDecimal("100.00"),
                Currency.USD, // Unsupported currency
                "card123",
                "Payment for test"
        );
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When
        ResultActions paymentResult = mockMvc.perform(put("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().is5xxServerError());
        assertThat(paymentRepository.findAll()).isEmpty();
    }
}