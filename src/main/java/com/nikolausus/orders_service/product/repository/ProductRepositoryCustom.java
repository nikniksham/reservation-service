package com.nikolausus.orders_service.product.repository;

import com.nikolausus.orders_service.product.dto.ProductDto;

import java.time.LocalDateTime;
import java.util.Map;

public interface ProductRepositoryCustom {

    Map<ProductDto, Long> findTopConfirmed(LocalDateTime from);

}
