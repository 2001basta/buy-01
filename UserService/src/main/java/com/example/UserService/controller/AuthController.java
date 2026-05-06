package com.example.UserService.controller;

import com.example.UserService.dto.SignupRequest;
import com.example.UserService.dto.UserLogin;
import com.example.UserService.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public Map<String, String> signup(@Valid @RequestBody SignupRequest req) {
        String token = authService.signup(req.name(), req.email(), req.password(), req.role());
        return Map.of("accessToken", token);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody UserLogin req) {
        String token = authService.login(req.email(), req.password());
        return Map.of("accessToken", token);
    }
}
