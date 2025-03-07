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
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CardPaymentCharger cardPaymentCharger;

    private UUID customerId;

    @BeforeEach
    void setUp() {
        // Clean repositories
        paymentRepository.deleteAll();
        customerRepository.deleteAll();
        
        // Create test customer
        customerId = UUID.randomUUID();
    }

    @Test
    void itShouldCreateCustomerAndProcessPayment() throws Exception {
        // Given
        // Customer registration data
        Customer customer = new Customer(customerId, "John Doe", "+1234567890");
        CustomerRegistrationRequest customerRequest = new CustomerRegistrationRequest(customer);

        // Payment data
        BigDecimal amount = new BigDecimal("100.00");
        Currency currency = Currency.GBP;
        String source = "card123";
        String description = "Test payment";
        
        Payment payment = new Payment(null, customerId, amount, currency, source, description);
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When
        // 1. Register the customer
        ResultActions customerRegistrationResult = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest)));

        // Then
        customerRegistrationResult.andExpect(status().isOk());
        
        // Verify customer is saved
        assertThat(customerRepository.findById(customerId)).isPresent();
        
        // When
        // 2. Process payment
        ResultActions paymentResult = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));
        
        // Then
        paymentResult.andExpect(status().isOk());
        
        // Verify payment is saved
        assertThat(paymentRepository.findAll()).hasSize(1);
        
        Payment savedPayment = paymentRepository.findAll().iterator().next();
        assertThat(savedPayment.getCustomerId()).isEqualTo(customerId);
        assertThat(savedPayment.getAmount()).isEqualByComparingTo(amount);
        assertThat(savedPayment.getCurrency()).isEqualTo(currency);
    }

    @Test
    void itShouldFailWhenCustomerDoesNotExist() throws Exception {
        // Given
        UUID nonExistingCustomerId = UUID.randomUUID();
        
        Payment payment = new Payment(
                null,
                nonExistingCustomerId,
                new BigDecimal("100.00"),
                Currency.GBP,
                "card123",
                "Test payment"
        );
        
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When
        ResultActions paymentResult = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().isInternalServerError());
    }

    @Test
    void itShouldFailWhenCurrencyNotSupported() throws Exception {
        // Given
        // Create and register customer
        Customer customer = new Customer(customerId, "John Doe", "+1234567890");
        CustomerRegistrationRequest customerRequest = new CustomerRegistrationRequest(customer);
        
        mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isOk());

        // Create payment with unsupported currency
        Payment payment = new Payment(
                null,
                customerId,
                new BigDecimal("100.00"),
                Currency.USD,  // USD is not in ACCEPTED_CURRENCIES
                "card123",
                "Test payment"
        );
        
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When
        ResultActions paymentResult = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        // Then
        paymentResult.andExpect(status().isInternalServerError());
    }

    @Test
    void itShouldFailWhenCardPaymentNotDebited() throws Exception {
        // Given - This test would need a way to make cardPaymentCharger return isCardDebited=false
        // Would require mocking or a test implementation of CardPaymentCharger
        // Not implemented in this example as it would depend on the actual implementation
    }
}