package com.sss.app.controller.files;

import com.sss.app.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Local-disk file storage for library images / org logos, chosen for v1 since
 * it needs no external credentials to stand up (S3 is a config swap later —
 * only the storage location changes, not the API shape or the callers).
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileStorageController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 8L * 1024 * 1024;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the 8MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, or WEBP images are allowed");
        }

        String extension = switch (contentType) {
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            default -> ".webp";
        };
        String storedName = UUID.randomUUID() + extension;

        try {
            Path targetDir = Path.of(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(storedName);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        return ResponseEntity.ok(Map.of("url", "/files/" + storedName));
    }
}
