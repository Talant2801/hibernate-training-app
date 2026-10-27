package org.nikita.hibernatebookpractice.integration;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    protected void assertQueryCount(long expected, Runnable action) {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        action.run();
        assertEquals(expected, statistics.getPrepareStatementCount());
    }

    @AfterEach
    void closeSessionFactory() {
        sessionFactory.close();
    }
}