package com.example.ApiGateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;

@Component
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
            .authorizeExchange(auth -> 
                auth
                .pathMatchers("/users/login", "/users/signup").permitAll()
                .anyExchange().authenticated()
            );
        return http.build();
    }
}