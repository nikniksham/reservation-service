package com.nikolausus.orders_service.exception;

public class ReservationCannotBeConfirmed extends RuntimeException {
    public ReservationCannotBeConfirmed(Long reservationId) {
        super("Reservation cannot be confirmed, id: " + reservationId);
    }
}
