package com.example.UserService.service;

import com.example.UserService.entity.User;
import com.example.UserService.outils.Role;
import com.example.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String signup(String name, String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Collections.singleton(Role.CLIENT))
                .provider("NORMAL")
                .build();

        userRepository.save(user);

        return jwtService.generateToken(user.getId(), user.getRoles());
    }

    public String login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid password");

        return jwtService.generateToken(user.getId(), user.getRoles());
    }
}