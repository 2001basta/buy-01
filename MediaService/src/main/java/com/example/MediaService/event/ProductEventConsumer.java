package com.example.MediaService.event;

import com.example.MediaService.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final MediaService mediaService;

    @KafkaListener(topics = "product-events", groupId = "media-service")
    public void onProductEvent(ProductEvent event) {
        if (event.productId() == null || event.productId().isBlank()) {
            return;
        }

        List<String> imageIds = event.imageIds() == null ? List.of() : event.imageIds();
        if ("DELETED".equals(event.eventType())) {
            mediaService.deleteAllForProduct(event.productId());
        } else if ("IMAGE_REMOVED".equals(event.eventType())) {
            mediaService.deleteAll(imageIds);
        } else if ("UPDATED".equals(event.eventType())) {
            mediaService.removeImagesDetachedFromProduct(event.productId(), imageIds);
        }
    }
}
