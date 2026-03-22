package com.nikolausus.orders_service.service;

import com.nikolausus.orders_service.dto.ProductDto;
import com.nikolausus.orders_service.entity.Product;
import com.nikolausus.orders_service.entity.Reservation;
import com.nikolausus.orders_service.exception.ReservationCannotBeConfirmed;
import com.nikolausus.orders_service.repository.ProductRepository;
import com.nikolausus.orders_service.repository.ReservationRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void expireReservationAndRestock(Long productId, LocalDateTime now) {
        List<Reservation> expired = reservationRepository.findAllExpiredReservation(productId, now);
        if (expired.isEmpty()) {return;}

        long total = expired.stream().mapToLong(Reservation::getQuantity).sum();

        expired.forEach(reservation -> reservation.setStatus(Reservation.Status.EXPIRED));

        Product product = findProduct(productId);
        product.setStock(product.getStock() + total);

    }

    @Transactional
    public void createReservation(Long productId, long quantity) {
        Product product = findProduct(productId);
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("The remaining product is less than requested");
        }

        product.setStock(product.getStock() - quantity);
    }

    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() != Reservation.Status.ACTIVE) {
            throw new ReservationCannotBeConfirmed(reservationId);
        }

        reservation.setStatus(Reservation.Status.CONFIRMED);
    }

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
