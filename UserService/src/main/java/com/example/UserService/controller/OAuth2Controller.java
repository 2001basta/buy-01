package com.example.UserService.controller;

import com.example.UserService.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class OAuth2Controller {

    private final SocialLoginService socialLoginService;

    @GetMapping("/users/oauth2/success")
    public Map<String, String> oauth2Success(HttpServletRequest request) {
        OAuth2User oAuth2User = (OAuth2User) request.getUserPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String provider = oAuth2User.getAttribute("iss") != null ? "GOOGLE" : "FACEBOOK";

        String token = socialLoginService.processOAuthPostLogin(email, name, provider);

        return Map.of("accessToken", token);
    }
}