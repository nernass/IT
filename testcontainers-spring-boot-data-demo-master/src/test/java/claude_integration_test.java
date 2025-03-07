package com.demo.integration;

import com.demo.MainApplication;
import com.demo.domain.Person;
import com.demo.service.PersonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MainApplication.class)
@ActiveProfiles("test")
@Transactional
public class PersonIntegrationTest {

    @Autowired
    private PersonService personService;

    @BeforeEach
    void setUp() {
        // Add test data
        personService.add("John Doe", 12345);
        personService.add("Jane Doe", 67890);
    }

    @Test
    void shouldLoadAllPersons() {
        // when
        List<Person> persons = personService.loadAll();

        // then
        assertThat(persons).isNotNull();
        assertThat(persons).hasSize(2);
        assertThat(persons.get(0).getFirstName()).isEqualTo("John Doe");
        assertThat(persons.get(0).getNationalId()).isEqualTo(12345);
    }

    @Test
    void shouldAddNewPerson() {
        // when
        personService.add("Test User", 11111);
        List<Person> persons = personService.loadAll();

        // then
        assertThat(persons).hasSize(3);
        Person addedPerson = persons.stream()
                .filter(p -> p.getNationalId() == 11111)
                .findFirst()
                .orElse(null);

        assertNotNull(addedPerson);
        assertEquals("Test User", addedPerson.getFirstName());
    }

    @Test
    void shouldHandleDuplicateNationalId() {
        // when/then
        assertThrows(Exception.class, () -> {
            personService.add("Duplicate User", 12345);
        });
    }
}