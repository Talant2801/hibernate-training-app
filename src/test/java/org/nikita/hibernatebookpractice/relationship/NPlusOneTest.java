package org.nikita.hibernatebookpractice.relationship;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikita.hibernatebookpractice.entity.Book;
import org.nikita.hibernatebookpractice.entity.Publisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NPlusOneTest {

    private static final int PUBLISHERS = 3;
    private static final int BOOKS_PER_PUBLISHER = 2;

    private SessionFactory sessionFactory;
    private Statistics statistics;

    @BeforeEach
    void setUp() {
        sessionFactory = new Configuration()
                .configure("hibernate-test.cfg.xml")
                .setProperty("hibernate.generate_statistics", "true")
                .buildSessionFactory();
        statistics = sessionFactory.getStatistics();

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("""
                truncate table book_loan, book_listing, books, publisher, reader
                restart identity cascade
                """).executeUpdate();

            for (int p = 1; p <= PUBLISHERS; p++) {
                Publisher publisher = Publisher.builder()
                        .name("Publisher " + p)
                        .country("UA")
                        .build();
                session.persist(publisher);
                for (int b = 1; b <= BOOKS_PER_PUBLISHER; b++) {
                    Book book = Book.builder()
                            .id("isbn-" + p + "-" + b)
                            .title("Book " + p + "." + b)
                            .build();
                    publisher.addBook(book);
                    session.persist(book);
                }
            }
            session.getTransaction().commit();
        }

        statistics.clear();
    }

    @AfterEach
    void tearDown() {
        sessionFactory.close();
    }

    @Test
    void loadingBooksThenPublishersCausesNPlusOne() {
        try (Session session = sessionFactory.openSession()) {
            List<Book> books = session
                    .createSelectionQuery("from Book", Book.class)
                    .getResultList();

            // publisher is LAZY: only the books query has run so far, publishers are proxies
            assertEquals(1, statistics.getPrepareStatementCount());
            assertFalse(Hibernate.isInitialized(books.get(0).getPublisher()));

            System.out.println("=== books loaded");
            for (Book book : books) {
                System.out.println(book.getTitle() + " → " + book.getPublisher().getName());
            }

            // 1 query for books + 1 per distinct publisher (the persistence context
            // dedupes repeats, so it's N = number of publishers, not books)
            assertEquals(1 + PUBLISHERS, statistics.getPrepareStatementCount());
        }
    }

    @Test
    void joinFetchLoadsBooksAndPublishersInOneQuery() {
        try (Session session = sessionFactory.openSession()) {
            List<Book> books = session
                    .createSelectionQuery("from Book b left join fetch b.publisher", Book.class)
                    .getResultList();

            assertEquals(PUBLISHERS * BOOKS_PER_PUBLISHER, books.size());
            assertTrue(Hibernate.isInitialized(books.get(0).getPublisher()));

            System.out.println("=== books loaded");
            for (Book book : books) {
                System.out.println(book.getTitle() + " → " + book.getPublisher().getName());
            }

            assertEquals(1, statistics.getPrepareStatementCount());
        }
    }
}
