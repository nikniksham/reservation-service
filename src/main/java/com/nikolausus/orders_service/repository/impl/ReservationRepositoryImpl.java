package com.nikolausus.orders_service.repository.impl;

import com.nikolausus.orders_service.entity.Reservation;
import com.nikolausus.orders_service.repository.ReservationRepositoryCustom;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Reservation> findAllExpiredReservation(Long productId, LocalDateTime now) {
        String jpql = """
        SELECT r
        FROM Reservation r
        WHERE r.status = 'ACTIVE' AND r.product.id = :productId AND r.expiresAt < :now
        """;

        return em.createQuery(jpql, Reservation.class)
                .setParameter("productId", productId)
                .setParameter("now", now)
                .getResultList();
    }

}
