package com.mb.livedataservice.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@NoArgsConstructor
@ToString(exclude = "author")
public class Book {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private Integer publishedYear;

    @JoinColumn(name = "author_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Author author;

    public Book(String title, Integer publishedYear) {
        this.title = title;
        this.publishedYear = publishedYear;
    }

    public Book(String title, Author author, Integer publishedYear) {
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
    }

    public boolean isEmpty() {
        return title == null && author == null && publishedYear == null;
    }
}
