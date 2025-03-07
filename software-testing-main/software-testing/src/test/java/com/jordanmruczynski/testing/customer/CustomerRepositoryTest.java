package com.jordanmruczynski.testing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository underTest;

    @Test
    void itShouldSaveCustomer() {
        // Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "John", "0000");
        // When
        underTest.save(customer);
        // Then
        Optional<Customer> optionalCustomer = underTest.findById(id);
        assertThat(optionalCustomer).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c).isEqualToComparingFieldByField(customer);
                });
    }

    @Test
    void itShouldCheckExistsCustomerByPhoneNumber() {
        // Given
        String phoneNumber = "123456789";
        Customer customer = new Customer(UUID.randomUUID(), "check", phoneNumber);
        underTest.save(customer);

        // When
        boolean expected = underTest.existsCustomerByPhoneNumber(phoneNumber);

        // Then
        assertThat(expected).isTrue();
    }

    @Test
    void itShouldFindCustomerByPhoneNumber() {
        // Given
        UUID id = UUID.randomUUID();
        String phoneNumber = "0001";
        Customer customer = new Customer(id, "name123", phoneNumber);
        underTest.save(customer);
        // When
        Optional<Customer> optionalCustomer = underTest.findCustomerByPhoneNumber(phoneNumber);
        // Then
        assertThat(optionalCustomer).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c).isEqualToComparingFieldByField(customer);
                });
    }

    @Test
    void itShouldNotFindCustomerByPhoneNumberWhenNumberDoesNotExists() {
        // Given
        UUID id = UUID.randomUUID();
        String phoneNumber = "0001";
        Customer customer = new Customer(id, "name123", phoneNumber);
        underTest.save(customer);
        // When
        Optional<Customer> optionalCustomer = underTest.findCustomerByPhoneNumber("3123123");
        // Then
        assertThat(optionalCustomer).isNotPresent();
    }

    @Test
    void itShouldNotSaveCustomerWhenNameIsNull() {
        // Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, null, "00400");

        // When
        // Then
        assertThatThrownBy(() -> underTest.save(customer))
                .hasMessageContaining("not-null property references a null or transient value : com.jordanmruczynski.testing.customer.Customer.name;")
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}