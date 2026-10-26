package org.nikita.hibernatebookpractice.session;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikita.hibernatebookpractice.entity.Book;

import org.nikita.hibernatebookpractice.integration.AbstractIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;


public class PersistenceContextTest extends AbstractIntegrationTest {

    private static final String ISBN = "978-0134685991";

    private static final String bookFirstName = "The Master and Margarita";

    private static final String bookLastName = "Harry Potter and the philosopher's stone";

    @BeforeEach
    void insertBook() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(Book.builder()
                    .id(ISBN)
                    .title(bookFirstName)
                    .build());
            session.getTransaction().commit();
        }
    }

    @Test
    void findSomeBooksTwiceInOneSession() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            Book b1 = session.find(Book.class, ISBN);
            Book b2 = session.find(Book.class, ISBN);

            assertTrue(b1 == b2);
            session.getTransaction().commit();
        }
    }

    @Test
    void dirtyEntityIsUpdatedOnCommitWithoutSave() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Book b1 = session.find(Book.class, ISBN);
            b1.setTitle(bookLastName);
            session.getTransaction().commit();
        }

        try (Session session = sessionFactory.openSession()) {
            Book b2 = session.find(Book.class, ISBN);
            assertEquals(bookLastName, b2.getTitle());
        }
    }

    @Test
    void entityIsNotUpdatedAfterEviction() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Book b1 = session.find(Book.class, ISBN);
            b1.setTitle(bookLastName);
            session.evict(b1);
            session.getTransaction().commit();
        }

        try (Session session = sessionFactory.openSession()) {
            Book b2 = session.find(Book.class, ISBN);
            assertEquals(bookFirstName, b2.getTitle());
        }
    }

    @Test
    void entityIsNotUpdatedAfterClear() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Book b1 = session.find(Book.class, ISBN);
            b1.setTitle(bookLastName);
            session.clear();
            session.getTransaction().commit();
        }

        try (Session session = sessionFactory.openSession()) {
            Book b2 = session.find(Book.class, ISBN);
            assertEquals(bookFirstName, b2.getTitle());
        }
    }

    @Test
    void flushedChangesAreInvisibleToOtherSessionsUntilCommit() {
        try (Session session1 = sessionFactory.openSession()) {
            session1.beginTransaction();
            Book book = session1.find(Book.class, ISBN);
            book.setTitle(bookLastName);
            session1.flush();                              // UPDATE sent, not committed

            // Another session looks while session1's transaction is still open
            try (Session session2 = sessionFactory.openSession()) {
                assertEquals(bookFirstName,
                        session2.find(Book.class, ISBN).getTitle());
            }

            session1.getTransaction().commit();            // now it's permanent
        }

        try (Session session3 = sessionFactory.openSession()) {
            assertEquals(bookLastName,
                    session3.find(Book.class, ISBN).getTitle());
        }
    }

    @Test
    void evictedValueIsNotEqualtoMerged() {
        Book detached;
        try (Session session = sessionFactory.openSession()) {
            detached = session.find(Book.class, ISBN);
        }

        detached.setTitle("A new title");

        Book merged;
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            merged = session.merge(detached);
            session.getTransaction().commit();
        }

        assertNotSame(merged, detached);

        try (Session session = sessionFactory.openSession()) {
            assertEquals("A new title", session.find(Book.class, ISBN).getTitle());
        }
    }
}
