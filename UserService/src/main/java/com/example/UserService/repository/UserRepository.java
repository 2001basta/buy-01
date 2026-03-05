package com.example.UserService.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.UserService.entity.User;

public interface UserRepository  extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
}
