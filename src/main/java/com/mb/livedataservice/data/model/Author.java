package com.mb.livedataservice.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@ToString(exclude = "books")
public class Author {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String country;

    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();

    public Author(String name, String country) {
        this.name = name;
        this.country = country;
    }

    public boolean isEmpty() {
        return name == null && country == null;
    }
}
