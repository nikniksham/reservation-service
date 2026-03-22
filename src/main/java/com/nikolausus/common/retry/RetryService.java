package com.nikolausus.common.retry;

import java.util.function.Supplier;

public interface RetryService {
    void runWithRetry(Runnable action);
    public <T> T runReturningWithRetry(Supplier<T> action);

}
