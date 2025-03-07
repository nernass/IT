package ru.jo4j.testexamples;

public class EmailValidator implements Validator<String> {
    @Override
    public boolean validate(String value) {
        return value.equals("test@test.com");
    }
}
