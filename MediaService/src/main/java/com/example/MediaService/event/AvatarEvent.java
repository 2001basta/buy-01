package com.example.MediaService.event;

public record AvatarEvent(
        String eventType,
        String userId,
        String oldAvatarId
) {}
