package com.nikolausus.orders_service.service.impl;

import com.nikolausus.orders_service.dto.ProductDto;
import com.nikolausus.orders_service.entity.Product;
import com.nikolausus.orders_service.entity.Reservation;
import com.nikolausus.orders_service.exception.ReservationCannotBeConfirmed;
import com.nikolausus.orders_service.repository.ProductRepository;
import com.nikolausus.orders_service.repository.ReservationRepository;
import com.nikolausus.orders_service.service.InventoryReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReservationServiceImpl implements InventoryReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void expireReservationAndRestock(Long productId, LocalDateTime now) {
        List<Reservation> expired = reservationRepository.findAllExpiredReservation(productId, now);
        if (expired.isEmpty()) {return;}

        long total = expired.stream().mapToLong(Reservation::getQuantity).sum();

        expired.forEach(reservation -> reservation.setStatus(Reservation.Status.EXPIRED));

        Product product = findProduct(productId);
        product.setStock(product.getStock() + total);

    }

    @Override
    @Transactional
    public void createReservation(Long productId, long quantity) {
        Product product = findProduct(productId);
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("The remaining product is less than requested");
        }

        LocalDateTime now = LocalDateTime.now();

        Reservation reservation = Reservation.builder()
                .product(product)
                .quantity(quantity)
                .status(Reservation.Status.ACTIVE)
                .createdAt(now)
                .expiresAt(Reservation.getExpiresTime(now))
                .build();

        reservationRepository.save(reservation);

        product.setStock(product.getStock() - quantity);
    }

    @Override
    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() != Reservation.Status.ACTIVE) {
            throw new ReservationCannotBeConfirmed(reservationId);
        }

        reservation.setStatus(Reservation.Status.CONFIRMED);
    }

    @Override
    @Transactional
    public ProductDto getProduct(Long productId) {
        Product product = findProduct(productId);

        return ProductDto.from(product);
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found, id: " + productId));
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found, id: " + reservationId));
    }

}
