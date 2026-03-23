package com.nikolausus.orders_service.service.impl;

import com.nikolausus.orders_service.dto.ProductDto;
import com.nikolausus.orders_service.entity.Product;
import com.nikolausus.orders_service.entity.Reservation;
import com.nikolausus.orders_service.exception.NotEnoughStockException;
import com.nikolausus.orders_service.exception.ReservationCannotBeCancelled;
import com.nikolausus.orders_service.exception.ReservationCannotBeConfirmed;
import com.nikolausus.orders_service.repository.ProductRepository;
import com.nikolausus.orders_service.repository.ReservationRepository;
import com.nikolausus.orders_service.service.InventoryReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public long createReservation(Long productId, long quantity) {
        Product product = findProduct(productId);
        if (product.getStock() < quantity) {
            throw new NotEnoughStockException();
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

        return reservation.getId();
    }

    @Override
    @Transactional
    public long createProduct(String name, long stock) {
        Product product = Product.builder()
                .name(name)
                .stock(stock)
                .build();

        productRepository.save(product);

        return product.getId();
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
    public void cancelReservation(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        if (reservation.getStatus() != Reservation.Status.ACTIVE) {
            throw new ReservationCannotBeCancelled(reservationId);
        }

        reservation.setStatus(Reservation.Status.CANCELLED);

        Product product = reservation.getProduct();
        product.setStock(product.getStock() + reservation.getQuantity());
    }

    @Override
    @Transactional
    public ProductDto getProduct(Long productId) {
        Product product = findProduct(productId);

        return ProductDto.from(product);
    }

    @Override
    @Transactional
    public Long getProductId(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        return reservation.getProduct().getId();
    }

    @Override
    public Map<ProductDto, Long> getTopReservedProduct(int count, LocalDateTime from) {
        Map<Product, Long> topReserved = productRepository.findTopConfirmed(from);
        return topReserved.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(count)
                .collect(Collectors.toMap(
                        entry -> ProductDto.from(entry.getKey()),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
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
