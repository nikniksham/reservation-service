package com.nikolausus.common.retry.impl;

import com.nikolausus.common.retry.RetryService;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class RetryServiceImpl implements RetryService {

    @Override
    public void runWithRetry(Runnable action) {
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
