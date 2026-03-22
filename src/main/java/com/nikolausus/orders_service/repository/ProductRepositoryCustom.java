package com.nikolausus.orders_service.repository;

import com.nikolausus.orders_service.dto.ProductDto;

import java.time.LocalDateTime;
import java.util.Map;

public interface ProductRepositoryCustom {

    Map<ProductDto, Long> findTopConfirmed(LocalDateTime from);

}
