package org.nikita.hibernatebookpractice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Publisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String country;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "publisher", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        books.add(book);
        book.setPublisher(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Publisher other)) return false;
        if ((this.getId() != null) && (this.getId().equals(other.getId()))) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
