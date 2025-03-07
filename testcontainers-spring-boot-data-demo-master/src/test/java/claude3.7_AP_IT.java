package com.demo.integration;

import com.demo.MainApplication;
import com.demo.domain.Person;
import com.demo.repository.PersonRepository;
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
public class PersonServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Test
    void shouldAddAndLoadPerson() {
        // Given
        String firstName = "John";
        int nationalId = 123456;

        // When
        personService.add(firstName, nationalId);

        // Then
        List<Person> loadedPersons = personService.loadAll();

        // Assertions
        assertNotNull(loadedPersons);
        assertFalse(loadedPersons.isEmpty());
        assertEquals(1, loadedPersons.size());

        Person loadedPerson = loadedPersons.get(0);
        assertEquals(firstName, loadedPerson.getFirstName());
        assertEquals(nationalId, loadedPerson.getNationalId());
    }

    @Test
    void shouldLoadAllPersons() {
        // Given
        personRepository.deleteAll(); // Clean up database

        personService.add("Alice", 100001);
        personService.add("Bob", 100002);
        personService.add("Charlie", 100003);

        // When
        List<Person> allPersons = personService.loadAll();

        // Then
        assertEquals(3, allPersons.size());
        assertTrue(allPersons.stream().anyMatch(p -> "Alice".equals(p.getFirstName())));
        assertTrue(allPersons.stream().anyMatch(p -> "Bob".equals(p.getFirstName())));
        assertTrue(allPersons.stream().anyMatch(p -> "Charlie".equals(p.getFirstName())));
    }

    @Test
    void shouldHandleEdgeCases() {
        // Given
        personRepository.deleteAll();

        // Test with empty string
        personService.add("", 999999);

        // Test with maximum integer value for national ID
        personService.add("MaxPerson", Integer.MAX_VALUE);

        // When
        List<Person> persons = personService.loadAll();

        // Then
        assertEquals(2, persons.size());

        // Verify empty string was properly stored
        assertTrue(persons.stream().anyMatch(p -> "".equals(p.getFirstName()) && p.getNationalId() == 999999));

        // Verify max integer value was properly stored
        assertTrue(persons.stream()
                .anyMatch(p -> "MaxPerson".equals(p.getFirstName()) && p.getNationalId() == Integer.MAX_VALUE));
    }
}