package com.example.MediaService.repository;

import com.example.MediaService.entity.Media;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findByOwnerIdAndIdIn(String ownerId, List<String> ids);
}
