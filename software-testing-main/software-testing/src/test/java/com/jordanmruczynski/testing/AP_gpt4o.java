package com.jordanmruczynski.testing.integration;

import com.jordanmruczynski.testing.customer.*;
import com.jordanmruczynski.testing.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private CardPaymentCharger cardPaymentCharger;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testCustomerRegistrationAndPaymentSuccess() throws Exception {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John Doe", "123456789");
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.existsCustomerByPhoneNumber(anyString())).thenReturn(false);

        mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customer\": {\"id\": \"" + customerId
                        + "\", \"name\": \"John Doe\", \"phoneNumber\": \"123456789\"}}"))
                .andExpect(status().isOk());

        Payment payment = new Payment(1L, customerId, new BigDecimal("100.00"), Currency.PLN, "card123",
                "Test payment");
        when(cardPaymentCharger.chargeCard(anyString(), any(BigDecimal.class), any(Currency.class), anyString()))
                .thenReturn(new CardPaymentCharge(true));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payment\": {\"customerId\": \"" + customerId
                        + "\", \"amount\": 100.00, \"currency\": \"PLN\", \"source\": \"card123\", \"description\": \"Test payment\"}}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testPaymentFailureDueToUnsupportedCurrency() throws Exception {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John Doe", "123456789");
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payment\": {\"customerId\": \"" + customerId
                        + "\", \"amount\": 100.00, \"currency\": \"USD\", \"source\": \"card123\", \"description\": \"Test payment\"}}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testCustomerNotFound() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"payment\": {\"customerId\": \"" + customerId
                        + "\", \"amount\": 100.00, \"currency\": \"PLN\", \"source\": \"card123\", \"description\": \"Test payment\"}}"))
                .andExpect(status().isInternalServerError());
    }
}