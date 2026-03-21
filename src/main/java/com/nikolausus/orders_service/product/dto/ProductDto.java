package com.nikolausus.orders_service.product.dto;

import com.nikolausus.orders_service.product.entity.Product;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductDto {
    Long id;
    String name;
    long stock;

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .stock(product.getStock())
                .build();
    }

}
