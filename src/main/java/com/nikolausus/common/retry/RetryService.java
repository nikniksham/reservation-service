package com.nikolausus.common.retry;

import java.util.function.Supplier;

public interface RetryService {
    void runWithRetry(Runnable action);
}
