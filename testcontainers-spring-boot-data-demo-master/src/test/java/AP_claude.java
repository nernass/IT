package com.demo.integration;

import com.demo.MainApplication;
import com.demo.domain.Person;
import com.demo.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = MainApplication.class)
@Testcontainers
public class PersonServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PersonService personService;

    @Test
    void shouldAddAndLoadPerson() {
        // Given
        String firstName = "John";
        int nationalId = 12345;

        // When
        personService.add(firstName, nationalId);
        List<Person> people = personService.loadAll();

        // Then
        assertThat(people).hasSize(1);
        Person savedPerson = people.get(0);
        assertThat(savedPerson.getFirstName()).isEqualTo(firstName);
        assertThat(savedPerson.getNationalId()).isEqualTo(nationalId);
    }

    @Test
    void shouldHandleMultiplePersons() {
        // Given
        personService.add("Alice", 11111);
        personService.add("Bob", 22222);
        personService.add("Charlie", 33333);

        // When
        List<Person> people = personService.loadAll();

        // Then
        assertThat(people).hasSize(3);
        assertThat(people).extracting(Person::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
    }

    @Test
    void shouldHandleInvalidInput() {
        // Given
        String firstName = null;
        int nationalId = -1;

        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            personService.add(firstName, nationalId);
        });
    }

    @Test
    void shouldLoadEmptyListWhenNoData() {
        // When
        List<Person> people = personService.loadAll();

        // Then
        assertThat(people).isEmpty();
    }
}