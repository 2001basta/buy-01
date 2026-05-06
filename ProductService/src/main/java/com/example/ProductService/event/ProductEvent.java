package com.example.ProductService.event;

import java.time.LocalDateTime;
import java.util.List;

public record ProductEvent(
        String eventType,
        String productId,
        String sellerId,
        String name,
        double price,
        List<String> imageIds,
        LocalDateTime timestamp
) {}
