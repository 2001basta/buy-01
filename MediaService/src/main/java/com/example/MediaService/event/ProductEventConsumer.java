package com.example.MediaService.event;

import com.example.MediaService.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final MediaService mediaService;

    @KafkaListener(topics = "product-events", groupId = "media-service")
    public void onProductEvent(ProductEvent event) {
        if (!"DELETED".equals(event.eventType()) || event.imageIds() == null || event.imageIds().isEmpty()) {
            return;
        }
        mediaService.deleteAll(event.imageIds());
    }
}
