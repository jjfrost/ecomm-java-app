package com.ecommerce.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories", schema = "ecomm")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq_generator")
    @SequenceGenerator(
            name = "category_seq_generator",
            sequenceName = "category_seq",
            schema = "ecomm",
            allocationSize = 1)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false, length = 150)
    private String name;

    protected Category() {
    }

    public Category(String name) {
        this.name = name.trim();
    }

    public void rename(String name) {
        this.name = name.trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
