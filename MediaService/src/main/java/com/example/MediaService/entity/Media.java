package com.example.MediaService.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    private String id;
    private String url;
    private String ownerId;
    private String productId;
    private String type;
    private long size;
    private String mimeType;
    private String storagePath;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
