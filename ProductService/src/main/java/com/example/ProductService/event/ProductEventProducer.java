package com.example.ProductService.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    static final String TOPIC = "product-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(ProductEvent event) {
        kafkaTemplate.send(TOPIC, event.productId(), event);
    }
}
