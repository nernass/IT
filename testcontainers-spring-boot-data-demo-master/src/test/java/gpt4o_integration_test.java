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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MainApplication.class)
@ActiveProfiles("test")
@Transactional
public class PersonServiceIntegrationTest {

    @Autowired
    private PersonService personService;

    @BeforeEach
    public void setUp() {
        // Setup code here
    }

    @Test
    public void testAddPerson() {
        personService.add("John Doe", 123456);
        List<Person> persons = personService.loadAll();
        assertFalse(persons.isEmpty());
        assertEquals("John Doe", persons.get(0).getFirstName());
        assertEquals(123456, persons.get(0).getNationalId());
    }

    @Test
    public void testLoadAllPersons() {
        personService.add("Jane Doe", 654321);
        List<Person> persons = personService.loadAll();
        assertFalse(persons.isEmpty());
        assertEquals(1, persons.size());
        assertEquals("Jane Doe", persons.get(0).getFirstName());
        assertEquals(654321, persons.get(0).getNationalId());
    }

    @Test
    public void testErrorHandling() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            personService.add(null, 0);
        });
        assertNotNull(exception);
    }
}