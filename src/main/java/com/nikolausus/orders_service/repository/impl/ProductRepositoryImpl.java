package com.nikolausus.orders_service.repository.impl;

import com.nikolausus.orders_service.dto.ProductDto;
import com.nikolausus.orders_service.entity.Product;
import com.nikolausus.orders_service.repository.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final EntityManager em;

    @Override
    public Map<ProductDto, Long> findTopConfirmed(LocalDateTime from) {
        String jpql = """
        SELECT r.product, COUNT(r.id)
        FROM Reservation r
        WHERE r.status = 'CONFIRMED'AND r.createdAt >= :from
        GROUP BY r.product
        """;

        List<Object[]> results = em.createQuery(jpql, Object[].class)
                .setParameter("from", from)
                .getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        row -> ProductDto.from((Product) row[0]),
                        row -> (Long) row[1]
                ));
    }

}
