package com.sss.app.controller.files;

import com.sss.app.service.files.CloudinaryService;
import com.sss.app.service.files.CloudinaryUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Generic image upload endpoint used by every module (org logos, hotel/
 * activity/destination galleries, ...) — storage itself is delegated to
 * CloudinaryService, so this controller stays a thin HTTP adapter and any
 * future storage-provider swap only touches that one service.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileStorageController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        CloudinaryUploadResult result = cloudinaryService.upload(file);
        return ResponseEntity.ok(Map.of("url", result.secureUrl(), "public_id", result.publicId()));
    }
}
