package com.jordanmruczynski.testing.customer;

import com.jordanmruczynski.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

class CustomerRegistrationServiceTest {

    private CustomerRegistrationService underTest;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @Mock
    private CustomerRepository customerRepository = mock(CustomerRepository.class);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService(customerRepository, phoneNumberValidator);
    }

    @Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        // Given
        String phoneNumber = "0000";
        Customer customer = new Customer(null, "Marian", phoneNumber);

        //r ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... no customer with phone number passed
        given(customerRepository.findCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        // Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualToIgnoringGivenFields(customer, "id");
        assertThat(customerArgumentCaptorValue.getId()).isNotNull();
    }

    @Test
    void itShouldSaveNewCustomer() {
        // Given
        String phoneNumber = "0000";
        Customer customer = new Customer(UUID.randomUUID(), "Marian", phoneNumber);

        //r ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... no customer with phone number passed
        given(customerRepository.findCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.empty());

        // Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualTo(customer);
    }

    @Test
    void itShouldnNotSaveNewCustomerWhenCustomerExists() {
        // Given
        String phoneNumber = "0000";
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Marian", phoneNumber);

        //r ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... an existing customer is returned
        given(customerRepository.findCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customer));

        // Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldThrowExceptionWhenPhoneNumberIsTaken() {
        String phoneNumber = "0000";
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Marian", phoneNumber);
        Customer customerTwo = new Customer(id, "Marian", "0123");

        //r ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... an existing customer is returned
        given(customerRepository.findCustomerByPhoneNumber(phoneNumber)).willReturn(Optional.of(customerTwo));

        // Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Phone number already taken");

        // Finally
        then(customerRepository).should(never()).save(any(Customer.class));


    }

    @Test
    void itShouldNotSaveNewCustomerWhenPhoneNumberIsInvalid() {
        // Given
        String phoneNumber = "0000";
        Customer customer = new Customer(UUID.randomUUID(), "Marian", phoneNumber);

        //r ... a request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // Valid phone number
        given(phoneNumberValidator.test(phoneNumber)).willReturn(false);

        // When
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Phone number " + phoneNumber + " is not valid");

        // Then
        then(customerRepository).shouldHaveNoInteractions();
    }
}