package com.nikolausus.orders_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Product product;

    @Column(nullable = false)
    private long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public enum Status {
        ACTIVE,
        CONFIRMED,
        EXPIRED,
        CANCELLED
    }

    public static final int expireDelayInMinutes = 10;

    public static LocalDateTime getExpiresTime(LocalDateTime now) {
        return now.plusMinutes(expireDelayInMinutes);
    }
}
