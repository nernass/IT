package com.jordanmruczynski.testing.payment;

import com.jordanmruczynski.testing.customer.Customer;
import com.jordanmruczynski.testing.customer.CustomerRegistrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void itShouldCreatePaymentSucessfully() throws Exception {
        // Given a customer
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Jordan", "+48111222333");

        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        // Register
        ResultActions customerRegistrationResultActions = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectToJson(customerRegistrationRequest))));

        // ... Payment
        long paymentId = 1L;
        Payment payment = new Payment(paymentId, customerId, new BigDecimal("10.00"), Currency.PLN, "x0x0x", "card");

        // ... Payment request
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // ... When payment is sent
        ResultActions paymentResultActons = mockMvc.perform(post("/api/v1/payment", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectToJson(paymentRequest)));

        // Then both customer registration and payment requests are 200 status codde
        customerRegistrationResultActions.andExpect(status().isOk());
        paymentResultActons.andExpect(status().isOk());


        // Payment is stored in db
        // TODO: Do not use paymentRepository, instead create and endpoint to retrieve payments for customers
        assertThat(paymentRepository.findById(paymentId)).isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p).isEqualToComparingFieldByField(payment);
                });
        // TODO: Ensure sms is delivered (Twillo)
    }

    private String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to json");
            return null;
        }
    }
}
