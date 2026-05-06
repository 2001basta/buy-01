package com.example.MediaService.controller;

import com.example.MediaService.dto.MediaResponse;
import com.example.MediaService.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<MediaResponse> upload(
            @RequestHeader("userId") String userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "productId", required = false) String productId) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.upload(file, userId, productId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> serve(@PathVariable String id) throws IOException {
        Resource resource = mediaService.serve(id);
        String mimeType = mediaService.getMimeType(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String id,
            @RequestHeader("userId") String userId) {
        mediaService.delete(id, userId);
    }

    // Called by ProductService to validate imageIds before attaching them.
    @GetMapping("/exists")
    public Map<String, Boolean> exists(@RequestParam List<String> ids) {
        return Map.of("allExist", mediaService.allExist(ids));
    }
}
