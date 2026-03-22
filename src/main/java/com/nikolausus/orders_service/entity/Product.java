package com.nikolausus.orders_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long stock;

    @Version
    private Long version;

}
