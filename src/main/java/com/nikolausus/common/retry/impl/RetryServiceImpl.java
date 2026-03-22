package com.nikolausus.common.retry.impl;

import com.nikolausus.common.retry.RetryService;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Service
public class RetryServiceImpl implements RetryService {

    private static final int MAX_ATTEMPTS = 5;

    /**
     * Универсальный метод повторения с ретраем для действий с результатом и без.
     * Если нужно просто выполнить Runnable, используем runReturningWithRetry(() -> { action.run(); return null; })
     */
    public <T> T runWithRetry(Supplier<T> action) {
        int attempt = 0;

        while (true) {
            try {
                return action.get(); // возвращаем результат типа T (может быть null)
            } catch (OptimisticLockException ex) {
                if (++attempt > MAX_ATTEMPTS) {
                    throw ex;
                }

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

    /** Обёртка для Runnable, чтобы не возвращать значение */
    @Override
    public void runWithRetry(Runnable action) {
        runWithRetry(() -> {
            action.run();
            return null;
        });
    }

    /** Обёртка для Supplier<T> */
    @Override
    public <T> T runReturningWithRetry(Supplier<T> action) {
        return runWithRetry(action);
    }

}
