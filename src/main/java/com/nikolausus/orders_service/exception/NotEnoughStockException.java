package com.nikolausus.orders_service.exception;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException() {
        super("The remaining product is less than requested");
    }
}
