package com.github.kaiwinter.testcontainers.wildfly.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

@Testcontainers
@ExtendWith(WildflyExtension.class)
public class UserServiceIntegrationTest {

    @Container
    private static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>("mariadb:10.5.8")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @PersistenceContext
    private EntityManager entityManager;

    @Inject