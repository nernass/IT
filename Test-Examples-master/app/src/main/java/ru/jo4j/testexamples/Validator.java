package ru.jo4j.testexamples;

public interface Validator<T> {
    boolean validate(T value);
}
