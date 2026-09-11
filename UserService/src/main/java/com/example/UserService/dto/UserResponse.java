package com.example.UserService.dto;

import com.example.UserService.entity.User;
import com.example.UserService.outils.Role;

import java.util.Set;

public record UserResponse(
        String id,
        String name,
        String email,
        Set<Role> roles,
        String avatar
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRoles(), user.getAvatar());
    }
}
