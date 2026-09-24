package org.nikita.hibernatebookpractice;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.nikita.hibernatebookpractice.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HibernateBookPracticeApplication {

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        configuration.configure();

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {

            Session session = sessionFactory.openSession();
            session.beginTransaction();

            Publisher publisher = Publisher.builder().name("O'Reilly").country("USA").build();
            Reader reader = Reader.builder().email("a@test.com").fullName("Test A").build();
            session.persist(publisher);
            session.persist(reader);


            session.getTransaction().commit();
        }
    }

}
