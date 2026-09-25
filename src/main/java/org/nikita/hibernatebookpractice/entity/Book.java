package org.nikita.hibernatebookpractice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.nikita.hibernatebookpractice.converter.PriceConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="books")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @Column(name="isbn")
    private String id;
    private String title;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "author_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "author_last_name"))
    })
    private AuthorName authorName;
    @Column(name="publish_date")
    private LocalDate publishDate;
    @Convert(converter = PriceConverter.class)
    private Price price;
    @Enumerated(EnumType.STRING)
    private Condition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;
}
