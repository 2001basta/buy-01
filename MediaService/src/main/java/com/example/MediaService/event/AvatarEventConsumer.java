package com.example.MediaService.event;

import com.example.MediaService.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AvatarEventConsumer {

    private final MediaService mediaService;

    @KafkaListener(
            topics = "avatar-events",
            groupId = "media-service-avatar",
            containerFactory = "avatarKafkaListenerContainerFactory")
    public void onAvatarEvent(AvatarEvent event) {
        if ("AVATAR_REPLACED".equals(event.eventType())
                && event.oldAvatarId() != null
                && event.userId() != null) {
            mediaService.deleteIfPresent(event.oldAvatarId(), event.userId());
        }
    }
}
