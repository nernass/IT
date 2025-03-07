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
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testCustomerRegistrationAndPayment() throws Exception {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "John Doe", "123456789");
        Payment payment = new Payment(null, customerId, BigDecimal.valueOf(100.00), Currency.PLN, "card123",
                "Test payment");
        PaymentRequest paymentRequest = new PaymentRequest(payment);
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        Mockito.when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        Mockito.when(cardPaymentCharger.chargeCard(Mockito.anyString(), Mockito.any(BigDecimal.class),
                Mockito.any(Currency.class), Mockito.anyString()))
                .thenReturn(new CardPaymentCharge(true));

        // Register new customer
        mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customer\":{\"id\":\"" + customerId
                        + "\",\"name\":\"John Doe\",\"phoneNumber\":\"123456789\"}}"))
                .andExpect(status().isOk());

        // Make a payment
        mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"payment\":{\"amount\":100.00,\"currency\":\"PLN\",\"source\":\"card123\",\"description\":\"Test payment\"}}"))
                .andExpect(status().isOk());
    }
}