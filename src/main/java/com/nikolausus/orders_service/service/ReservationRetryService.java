package com.nikolausus.orders_service.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ReservationRetryService {

    private final InventoryReservationService inventoryService;

    public void expireReservationAndRestockWithRetry(Long productId, LocalDateTime now) {
        executeWithRetry(() -> inventoryService.expireReservationAndRestock(productId, now));
    }

    public void createReservationWithRetry(Long productId, long quantity) {
        executeWithRetry(() -> inventoryService.createReservation(productId, quantity));
    }

    private void executeWithRetry(Runnable action) {
        int attempt = 0;
        int maxAttempts = 5;
        while (true) {
            try {
                action.run();
                return;
            } catch (OptimisticLockException ex) {
                if (++attempt > maxAttempts) {throw ex;}

                // backoff с jitter
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(50, 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
