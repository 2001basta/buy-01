package com.example.UserService.service;

import com.example.UserService.entity.User;
import com.example.UserService.outils.Role;
import com.example.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public String processOAuthPostLogin(String email, String name, String provider) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            // Create new user for first-time social login
            user = User.builder()
                    .email(email)
                    .name(name)
                    .roles(Collections.singleton(Role.CLIENT))
                    .provider(provider.toUpperCase())
                    .build();
            userRepository.save(user);
        }

        return jwtService.generateToken(user.getId(), user.getRoles());
    }
}