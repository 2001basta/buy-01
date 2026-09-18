package com.example.MediaService.service;

import com.example.MediaService.dto.MediaResponse;
import com.example.MediaService.entity.Media;
import com.example.MediaService.exception.FileTooLargeException;
import com.example.MediaService.exception.InvalidFileTypeException;
import com.example.MediaService.exception.NotFoundException;
import com.example.MediaService.exception.UnauthorizedActionException;
import com.example.MediaService.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final long MAX_SIZE = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png");
    private static final Tika TIKA = new Tika();

    private final MediaRepository mediaRepository;

    @Value("${upload.dir}")
    private String uploadDir;

    public MediaResponse upload(MultipartFile file, String ownerId, String productId) throws IOException {
        if (file.getSize() > MAX_SIZE) {
            throw new FileTooLargeException();
        }

        String mimeType = TIKA.detect(file.getInputStream());
        if (!ALLOWED_MIME.contains(mimeType)) {
            throw new InvalidFileTypeException(mimeType);
        }

        if (productId != null && !productId.isBlank()) {
            long currentImageCount = mediaRepository.countByProductId(productId);
            if (currentImageCount >= 5) {
                throw new IllegalArgumentException("You cannot upload more than 5 images for a product");
            }
        }

        // Pre-generate ID so we can build the URL before saving (avoids double save)
        String mediaId = new ObjectId().toHexString();

        String filename = mediaId + extensionFor(mimeType);
        Path directory = resolveDirectory(ownerId, productId);
        Files.createDirectories(directory);
        Path filePath = directory.resolve(filename);
        file.transferTo(filePath.toFile());

        Media media = Media.builder()
                .id(mediaId)
                .ownerId(ownerId)
                .productId(productId)
                .type("IMAGE")
                .size(file.getSize())
                .mimeType(mimeType)
                .storagePath(filePath.toString())
                .url("/api/media/" + mediaId)
                .build();

        return MediaResponse.from(mediaRepository.save(media));
    }

    public Resource serve(String id) throws MalformedURLException {
        Media media = findOrThrow(id);
        Path filePath = Paths.get(media.getStoragePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            throw new NotFoundException("File not found on disk for media: " + id);
        }
        return resource;
    }

    public String getMimeType(String id) {
        return findOrThrow(id).getMimeType();
    }

    public void delete(String id, String currentUserId) {
        Media media = findOrThrow(id);
        if (!media.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedActionException("You do not own this resource");
        }
        Path filePath = Paths.get(media.getStoragePath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
        mediaRepository.delete(media);
    }

    public boolean allExist(List<String> ids, String ownerId) {
        List<Media> found = mediaRepository.findAllById(ids);
        return found.size() == ids.size()
                && found.stream().allMatch(m -> m.getOwnerId().equals(ownerId));
    }

    // Best-effort cleanup triggered by a product-deletion event: no owner to check
    // against here, and a missing/already-gone media id is not an error.
    public void deleteAll(List<String> ids) {
        List<Media> found = mediaRepository.findAllById(ids);
        deleteMediaFilesAndRecords(found);
    }

    public void deleteAllForProduct(String productId) {
        deleteMediaFilesAndRecords(mediaRepository.findByProductId(productId));
    }

    // Product updates carry the complete current image list. Remove media that is no longer attached.
    public void removeImagesDetachedFromProduct(String productId, List<String> currentImageIds) {
        Set<String> attachedIds = new HashSet<>(currentImageIds);
        List<Media> detached = mediaRepository.findByProductId(productId).stream()
                .filter(media -> !attachedIds.contains(media.getId()))
                .toList();

        deleteMediaFilesAndRecords(detached);
    }

    private void deleteMediaFilesAndRecords(List<Media> mediaItems) {
        for (Media media : mediaItems) {
            try {
                Files.deleteIfExists(Paths.get(media.getStoragePath()));
            } catch (IOException ignored) {
            }
        }
        mediaRepository.deleteAll(mediaItems);
    }

    private Media findOrThrow(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media not found: " + id));
    }

    private Path resolveDirectory(String ownerId, String productId) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path directory;
        if (productId != null && !productId.isBlank()) {
            directory = root.resolve("products").resolve(productId).normalize();
        } else {
            directory = root.resolve("users").resolve(ownerId).normalize();
        }
        if (!directory.startsWith(root)) {
            throw new NotFoundException("Invalid storage path");
        }
        return directory;
    }

    private String extensionFor(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            default           -> "";
        };
    }
}
