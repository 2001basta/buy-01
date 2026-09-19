package com.example.UserService.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarEventProducer {

    static final String TOPIC = "avatar-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(AvatarEvent event) {
        kafkaTemplate.send(TOPIC, event.userId(), event);
    }
}
