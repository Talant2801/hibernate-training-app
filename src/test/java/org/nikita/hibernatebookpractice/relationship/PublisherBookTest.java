package org.nikita.hibernatebookpractice.relationship;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikita.hibernatebookpractice.entity.Book;
import org.nikita.hibernatebookpractice.entity.Publisher;
import org.nikita.hibernatebookpractice.integration.AbstractIntegrationTest;

import static org.junit.jupiter.api.Assertions.*;

public class PublisherBookTest extends AbstractIntegrationTest {

    private static final String ISBN = "978-0134685991";

    private static final String OTHER_ISBN = "978-0134757599";

    private static final String PUBLISHER_NAME = "Addison-Wesley";

    private Session session;

    private Long publisherId;

    @BeforeEach
    void setUp() {
        session = sessionFactory.openSession();
        session.beginTransaction();

        Publisher publisher = Publisher.builder()
                .name(PUBLISHER_NAME)
                .country("US")
                .build();
        session.persist(publisher);
        session.persist(Book.builder()
                .id(ISBN)
                .title("Effective Java")
                .publisher(publisher)
                .build());
        session.getTransaction().commit();
        publisherId = publisher.getId();

        // start every test with an empty persistence context and zeroed counters
        session.clear();
        sessionFactory.getStatistics().clear();
    }

    @AfterEach
    void tearDown() {
        session.close();
    }

    // Step 1: @ManyToOne(fetch = LAZY) — find() selects only the book,
    // the publisher SELECT runs on first access to a non-id property
    @Test
    void publisherIsLoadedLazily() {
        Statistics statistics = sessionFactory.getStatistics();

        Book book = session.find(Book.class, ISBN);

        assertEquals(1, statistics.getPrepareStatementCount());
        assertFalse(Hibernate.isInitialized(book.getPublisher()));

        System.out.println("=== accessing publisher name");
        assertEquals(PUBLISHER_NAME, book.getPublisher().getName());

        assertEquals(2, statistics.getPrepareStatementCount());
    }

    // Step 2: Book.toString -> publisher.toString -> books.toString -> Book.toString ...
    // overflows unless one side is excluded
    @Test
    void toStringAndHashCodeOnLoadedPublisherDoNotRecurse() {
        Publisher publisher = session.find(Publisher.class, publisherId);
        Book book = publisher.getBooks().get(0);

        System.out.println(assertDoesNotThrow(publisher::toString));
        System.out.println(assertDoesNotThrow(book::toString));
        assertDoesNotThrow(publisher::hashCode);
        assertDoesNotThrow(book::hashCode);
    }

    // Step 2: equality is based on a stable key, not on every field
    @Test
    void equalityUsesBusinessKeys() {
        Book book = session.find(Book.class, ISBN);

        assertEquals(Book.builder().id(ISBN).build(), book);
        assertEquals(Publisher.builder().name(PUBLISHER_NAME).build(),
                session.find(Publisher.class, publisherId));
    }

    // Step 3: Publisher.books is mappedBy (inverse side) — Hibernate ignores it when writing,
    // only Book.publisher (owning side) decides the value of books.publisher_id
    @Test
    void addingOnlyToInverseSideDoesNotWriteForeignKey() {
        session.beginTransaction();
        Publisher publisher = session.find(Publisher.class, publisherId);
        Book book = Book.builder().id(OTHER_ISBN).title("Refactoring").build();
        publisher.getBooks().add(book);
        session.persist(book);
        session.getTransaction().commit();
        session.clear();

        assertNull(session.find(Book.class, OTHER_ISBN).getPublisher());
    }

    @Test
    void addBookSetsOwningSideAndWritesForeignKey() {
        session.beginTransaction();
        Publisher publisher = session.find(Publisher.class, publisherId);
        Book book = Book.builder().id(OTHER_ISBN).title("Refactoring").build();
        publisher.addBook(book);
        session.persist(book);
        session.getTransaction().commit();
        session.clear();

        Book loaded = session.find(Book.class, OTHER_ISBN);
        assertNotNull(loaded.getPublisher());
        assertEquals(publisherId, loaded.getPublisher().getId());
    }

    // Step 4: orphanRemoval = true — a book removed from the collection is DELETEd
    // (without it, nothing happens: the book row and its publisher_id stay untouched)
    @Test
    void bookRemovedFromCollectionIsDeletedAsOrphan() {
        session.beginTransaction();
        Publisher publisher = session.find(Publisher.class, publisherId);
        publisher.getBooks().remove(publisher.getBooks().get(0));
        session.getTransaction().commit();
        session.clear();

        assertNull(session.find(Book.class, ISBN));
    }

    // Step 4: cascade = REMOVE — removing the publisher removes its books first
    // (without it, deleting the publisher violates the books.publisher_id FK)
    @Test
    void removingPublisherCascadesToBooks() {
        session.beginTransaction();
        session.remove(session.find(Publisher.class, publisherId));
        session.getTransaction().commit();
        session.clear();

        assertNull(session.find(Publisher.class, publisherId));
        assertNull(session.find(Book.class, ISBN));
    }
}
