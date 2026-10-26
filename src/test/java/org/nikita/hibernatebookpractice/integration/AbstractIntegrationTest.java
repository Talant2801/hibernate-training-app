package org.nikita.hibernatebookpractice.integration;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.postgresql.PostgreSQLContainer;

public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17");

    static {
        POSTGRES.start();
    }

    protected SessionFactory sessionFactory;

    @BeforeEach
    void buildSessionFactory() {
        sessionFactory = new Configuration()
                .configure("hibernate-test.cfg.xml")
                .setProperty("hibernate.connection.username", POSTGRES.getUsername())
                .setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl())
                .setProperty("hibernate.connection.password", POSTGRES.getPassword())
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.generate_statistics", "true")
                .buildSessionFactory();

        sessionFactory.getSchemaManager().truncateMappedObjects();
    }

    @AfterEach
    void closeSessionFactory() {
        sessionFactory.close();
    }
}