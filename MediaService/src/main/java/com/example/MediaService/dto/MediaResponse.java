package com.example.MediaService.dto;

import com.example.MediaService.entity.Media;

import java.time.LocalDateTime;

public record MediaResponse(
        String id,
        String url,
        String ownerId,
        String productId,
        String mimeType,
        long size,
        LocalDateTime createdAt
) {
    public static MediaResponse from(Media m) {
        return new MediaResponse(
                m.getId(),
                m.getUrl(),
                m.getOwnerId(),
                m.getProductId(),
                m.getMimeType(),
                m.getSize(),
                m.getCreatedAt()
        );
    }
}
