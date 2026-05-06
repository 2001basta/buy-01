package com.example.ProductService.dto;

import com.example.ProductService.entity.Product;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String description,
        double price,
        String sellerId,
        List<String> imageIds,
        LocalDateTime createdAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getSellerId(),
                p.getImageIds(),
                p.getCreatedAt()
        );
    }
}
