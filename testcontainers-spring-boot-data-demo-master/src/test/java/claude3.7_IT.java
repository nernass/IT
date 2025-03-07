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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MainApplication.class)
@Testcontainers
public class PersonIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("integration-tests-db")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PersonService personService;

    @Test
    void shouldAddAndRetrievePerson() {
        // Given
        String firstName = "John";
        int nationalId = 12345;

        // When
        personService.add(firstName, nationalId);
        List<Person> allPersons = personService.loadAll();

        // Then
        assertFalse(allPersons.isEmpty());
        Person savedPerson = allPersons.stream()
                .filter(p -> p.getFirstName().equals(firstName) && p.getNationalId() == nationalId)
                .findFirst()
                .orElse(null);

        assertNotNull(savedPerson);
        assertEquals(firstName, savedPerson.getFirstName());
        assertEquals(nationalId, savedPerson.getNationalId());
    }

    @Test
    void shouldLoadEmptyListWhenNoPersonsAdded() {
        // Given an empty database (assuming no other tests have added data)

        // When
        List<Person> initialPersons = personService.loadAll();

        // Then
        assertTrue(initialPersons.isEmpty());
    }

    @Test
    void shouldAddMultiplePersons() {
        // Given
        personService.add("Alice", 11111);
        personService.add("Bob", 22222);
        personService.add("Charlie", 33333);

        // When
        List<Person> persons = personService.loadAll();

        // Then
        assertEquals(3, persons.size());
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals("Alice") && p.getNationalId() == 11111));
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals("Bob") && p.getNationalId() == 22222));
        assertTrue(persons.stream().anyMatch(p -> p.getFirstName().equals("Charlie") && p.getNationalId() == 33333));
    }
}