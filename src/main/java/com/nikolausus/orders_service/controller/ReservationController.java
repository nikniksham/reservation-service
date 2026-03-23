package com.nikolausus.orders_service.controller;

import com.nikolausus.common.retry.RetryService;
import com.nikolausus.orders_service.dto.ProductDto;
import com.nikolausus.orders_service.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final InventoryReservationService inventoryReservationService;
    private final RetryService retryService;

    @PostMapping("/reservations")
    public ResponseEntity<String> createReservation(
            @RequestParam Long productId,
            @RequestParam long quantity
    ) {
        expireReservations(productId);

        long reservationId = retryService.runReturningWithRetry(
                () -> inventoryReservationService.createReservation(productId, quantity));
        return ResponseEntity.ok("" + reservationId);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<String> confirmReservation(
            @PathVariable Long reservationId
    ) {
        expireReservations(inventoryReservationService.getProductId(reservationId));

        inventoryReservationService.confirmReservation(reservationId);

        return ResponseEntity.ok("Reservation confirmed successfully");
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<String> cancelReservation(
            @PathVariable Long reservationId
    ) {
        expireReservations(inventoryReservationService.getProductId(reservationId));

        retryService.runWithRetry(() -> inventoryReservationService.cancelReservation(reservationId));

        return ResponseEntity.ok("Reservation cancelled successfully");
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDto> getProduct(
            @PathVariable Long productId
    ) {
        expireReservations(productId);

        return ResponseEntity.ok(inventoryReservationService.getProduct(productId));
    }

    @GetMapping("/products/top-reserved")
    public ResponseEntity<Map<ProductDto, Long>> getTopReservedProducts() {
        return ResponseEntity.ok(
                inventoryReservationService.getTopReservedProduct(5, LocalDateTime.now().minusDays(1))
        );
    }


    private void expireReservations(Long productId) {
        retryService.runWithRetry(
                () -> inventoryReservationService.expireReservationAndRestock(productId, LocalDateTime.now()));
    }

}
