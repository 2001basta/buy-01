package com.example.UserService.event;

public record AvatarEvent(
        String eventType,
        String userId,
        String oldAvatarId
) {}
