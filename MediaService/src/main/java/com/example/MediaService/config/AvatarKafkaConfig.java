package com.example.MediaService.config;

import com.example.MediaService.event.AvatarEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AvatarKafkaConfig {

    @Bean
    public ConsumerFactory<String, AvatarEvent> avatarConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        JsonDeserializer<AvatarEvent> valueDeserializer = new JsonDeserializer<>(AvatarEvent.class);
        valueDeserializer.setUseTypeHeaders(false);
        valueDeserializer.addTrustedPackages("com.example.MediaService.event");

        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "media-service-avatar");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AvatarEvent> avatarKafkaListenerContainerFactory(
            ConsumerFactory<String, AvatarEvent> avatarConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, AvatarEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(avatarConsumerFactory);
        return factory;
    }
}
