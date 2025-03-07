package com.jordanmruczynski.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordanmruczynski.testing.customer.*;
import com.jordanmruczynski.testing.payment.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PaymentSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static UUID testCustomerId;
    private static final String TEST_PHONE = "1234567890";

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        paymentRepository.deleteAll();
        testCustomerId = UUID.randomUUID();
    }

    @Test
    @Order(1)
    @DisplayName("Should successfully register a new customer")
    void shouldRegisterNewCustomer() throws Exception {
        // Given
        Customer customer = new Customer(testCustomerId, "John Doe", TEST_PHONE);
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // When & Then
        mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(customerRepository.findById(testCustomerId)).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("Should process payment for registered customer")
    void shouldProcessPaymentForRegisteredCustomer() throws Exception {
        // Given
        // First register customer
        Customer customer = new Customer(testCustomerId, "John Doe", TEST_PHONE);
        customerRepository.save(customer);

        Payment payment = new Payment(
                null,
                testCustomerId,
                new BigDecimal("100.00"),
                Currency.PLN,
                "card123",
                "Test payment");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When & Then
        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    @Test
    @Order(3)
    @DisplayName("Should fail payment for non-existent customer")
    void shouldFailPaymentForNonExistentCustomer() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Payment payment = new Payment(
                null,
                nonExistentId,
                new BigDecimal("50.00"),
                Currency.PLN,
                "card123",
                "Test payment");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When & Then
        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError());

        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Should fail payment with unsupported currency")
    void shouldFailPaymentWithUnsupportedCurrency() throws Exception {
        // Given
        // First register customer
        Customer customer = new Customer(testCustomerId, "John Doe", TEST_PHONE);
        customerRepository.save(customer);

        Payment payment = new Payment(
                null,
                testCustomerId,
                new BigDecimal("75.00"),
                Currency.USD, // Unsupported currency
                "card123",
                "Test payment");
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // When & Then
        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isInternalServerError());

        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("Should reject duplicate customer registration")
    void shouldRejectDuplicateCustomerRegistration() throws Exception {
        // Given
        Customer customer = new Customer(testCustomerId, "John Doe", TEST_PHONE);
        customerRepository.save(customer);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                new Customer(UUID.randomUUID(), "Jane Doe", TEST_PHONE) // Same phone number
        );

        // When & Then
        mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Should process multiple payments for same customer")
    void shouldProcessMultiplePaymentsForSameCustomer() throws Exception {
        // Given
        Customer customer = new Customer(testCustomerId, "John Doe", TEST_PHONE);
        customerRepository.save(customer);

        Payment payment1 = new Payment(null, testCustomerId, new BigDecimal("100.00"), Currency.PLN, "card123",
                "Payment 1");
        Payment payment2 = new Payment(null, testCustomerId, new BigDecimal("200.00"), Currency.PLN, "card123",
                "Payment 2");

        // When & Then
        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PaymentRequest(payment1))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PaymentRequest(payment2))))
                .andExpect(status().isOk());

        assertThat(paymentRepository.findAll()).hasSize(2);
    }
}