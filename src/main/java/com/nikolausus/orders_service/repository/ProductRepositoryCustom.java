package com.nikolausus.orders_service.repository;

import com.nikolausus.orders_service.entity.Product;

import java.time.LocalDateTime;
import java.util.Map;

public interface ProductRepositoryCustom {

    Map<Product, Long> findTopConfirmed(LocalDateTime from);

}
