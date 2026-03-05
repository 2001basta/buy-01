package com.example.UserService.controller;

import com.example.UserService.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody Map<String, String> body) {
        String token = authService.signup(body.get("name"), body.get("email"), body.get("password"));
        return Map.of("accessToken", token);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("email"), body.get("password"));
        return Map.of("accessToken", token);
    }
}