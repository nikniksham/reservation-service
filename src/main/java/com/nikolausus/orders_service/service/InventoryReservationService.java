package com.nikolausus.orders_service.service;

import com.nikolausus.orders_service.dto.ProductDto;

import java.time.LocalDateTime;
import java.util.Map;

public interface InventoryReservationService {

    public void expireReservationAndRestock(Long productId, LocalDateTime now);

    public long createReservation(Long productId, long quantity);

    public long createProduct(String name, long stock);

    public void confirmReservation(Long reservationId);

    public ProductDto getProduct(Long productId);

    public Long getProductId(Long reservationId);

    public Map<ProductDto, Long> getTopReservedProduct(int count, LocalDateTime from);

}
