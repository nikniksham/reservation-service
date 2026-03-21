package com.nikolausus.orders_service.reservation.repository.impl;

import com.nikolausus.orders_service.product.entity.Product;
import com.nikolausus.orders_service.reservation.entity.Reservation;
import com.nikolausus.orders_service.reservation.repository.ReservationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final EntityManager em;

    @Override
    public long markExpiredReservationAndGetQuantitySum(Long productId, LocalDateTime now) {
        String jpql = """
        SELECT r
        FROM Reservation r
        WHERE r.status = 'ACTIVE' AND r.product.id = :productId AND r.expiresAt < :now
        """;

        List<Reservation> expired = em.createQuery(jpql, Reservation.class)
                .setParameter("productId", productId)
                .setParameter("now", now)
                .getResultList();

        expired.forEach(r -> r.setStatus(Reservation.Status.EXPIRED));

        return expired.stream()
                .mapToLong(Reservation::getQuantity)
                .sum();
    }

}
