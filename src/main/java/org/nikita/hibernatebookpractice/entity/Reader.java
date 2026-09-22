package org.nikita.hibernatebookpractice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reader_seq")
    @SequenceGenerator(name = "reader_seq", sequenceName = "reader_id_seq", allocationSize = 1)
    private Long id;

    private String email;

    @Column(name = "full_name")
    private String fullName;
}
