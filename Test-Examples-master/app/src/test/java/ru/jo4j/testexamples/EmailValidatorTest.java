package ru.jo4j.testexamples;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidatorTest {
    private Validator<String> validator;

    @Before
    public void before() {
        validator = new EmailValidator();
    }

    @Test
    public void whenEmailIsNotValid() {
        String email = "";
        assertThat(validator.validate(email)).isFalse();

    }

    @Test
    public void whenEmailIsNotValid2() {
        String email = "Shadow";
        assertThat(validator.validate(email)).isFalse();

    }

    @Test
    public void whenEmailIsValid() {
        String email = "test@test.com";
        assertThat(validator.validate(email)).isTrue();
    }
}