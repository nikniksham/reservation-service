package com.nikolausus.orders_service.product.repository;

import com.nikolausus.orders_service.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

}
