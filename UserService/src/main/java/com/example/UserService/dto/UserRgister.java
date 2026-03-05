package com.example.UserService.dto;

import org.springframework.web.multipart.MultipartFile;

import com.example.UserService.outils.Role;

public record UserRgister(
    String name,
    String email,
    String password,
    Role role,
    MultipartFile avatar
) {}
