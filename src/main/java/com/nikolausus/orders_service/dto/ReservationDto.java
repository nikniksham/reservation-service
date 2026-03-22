package com.nikolausus.orders_service.dto;

import com.nikolausus.orders_service.entity.Reservation;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ReservationDto {

    Long id;
    ProductDto productDto;
    long quantity;
    Reservation.Status status;
    LocalDateTime createdAt;
    LocalDateTime expiresAt;

    public static ReservationDto from(Reservation reservation) {
        return ReservationDto.builder()
                .id(reservation.getId())
                .productDto(ProductDto.from(reservation.getProduct()))
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .expiresAt(reservation.getExpiresAt())
                .build();
    }

}
