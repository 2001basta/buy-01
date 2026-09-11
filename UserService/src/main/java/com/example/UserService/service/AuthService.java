package com.example.UserService.service;

import com.example.UserService.entity.User;
import com.example.UserService.dto.ProfileUpdateRequest;
import com.example.UserService.dto.UserResponse;
import com.example.UserService.outils.Role;
import com.example.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public String signup(String name, String email, String password, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role assignedRole = (role != null) ? role : Role.CLIENT;

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Collections.singleton(assignedRole))
                .provider("NORMAL")
                .build();

        userRepository.save(user);
        return jwtService.generateToken(user.getId(), user.getRoles());
    }

    public String login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) throw new RuntimeException("Invalid credentials");

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid credentials");

        return jwtService.generateToken(user.getId(), user.getRoles());
    }

    public UserResponse getProfile(String userId) {
        return UserResponse.from(findById(userId));
    }

    public UserResponse updateProfile(String userId, ProfileUpdateRequest request) {
        User user = findById(userId);
        userRepository.findByEmail(request.email())
                .filter(existing -> !existing.getId().equals(userId))
                .ifPresent(existing -> { throw new RuntimeException("Email already exists"); });
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase());
        if (request.avatar() != null) user.setAvatar(request.avatar());
        return UserResponse.from(userRepository.save(user));
    }

    private User findById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
