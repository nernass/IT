package com.demo.service;

import com.demo.MainApplication;
import com.demo.domain.Person;
import com.demo.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = MainApplication.class)
public class PersonServiceIntegrationTest {

    @MockBean
    private PersonRepository personRepository;

    @Autowired
    @InjectMocks
    private PersonServiceImpl personService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadAllSuccess() {
        Person person1 = new Person();
        person1.setId(1);
        person1.setFirstName("John");
        person1.setNationalId(12345);

        Person person2 = new Person();
        person2.setId(2);
        person2.setFirstName("Jane");
        person2.setNationalId(67890);

        when(personRepository.findAll()).thenReturn(Arrays.asList(person1, person2));

        List<Person> persons = personService.loadAll();
        assertEquals(2, persons.size());
        assertEquals("John", persons.get(0).getFirstName());
        assertEquals("Jane", persons.get(1).getFirstName());
    }

    @Test
    public void testAddPersonSuccess() {
        Person person = new Person();
        person.setFirstName("John");
        person.setNationalId(12345);

        when(personRepository.save(any(Person.class))).thenReturn(person);

        personService.add("John", 12345);

        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    public void testAddPersonFailure() {
        doThrow(new RuntimeException("Database error")).when(personRepository).save(any(Person.class));

        try {
            personService.add("John", 12345);
        } catch (Exception e) {
            assertEquals("Database error", e.getMessage());
        }

        verify(personRepository, times(1)).save(any(Person.class));
    }
}