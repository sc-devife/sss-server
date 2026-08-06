package com.sss.app.service.files;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.sss.app.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generic, reusable Cloudinary upload/delete service — used by every module
 * that stores an image (org logos, hotel/activity/escape point galleries, and
 * anything added later) instead of each having its own storage logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 8L * 1024 * 1024;
    private static final String DEFAULT_FOLDER = "sss";
    private static final String CLOUDINARY_HOST_MARKER = "res.cloudinary.com";

    private final Cloudinary cloudinary;

    public CloudinaryUploadResult upload(MultipartFile file) {
        return upload(file, DEFAULT_FOLDER);
    }

    /** @param folder Cloudinary folder to upload into, e.g. "sss/hotels" — lets callers keep assets organized without needing their own upload logic. */
    public CloudinaryUploadResult upload(MultipartFile file, String folder) {
        validate(file);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image"
            ));
            String secureUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            log.info("Uploaded image to Cloudinary: folder={}, publicId={}", folder, publicId);
            return new CloudinaryUploadResult(secureUrl, publicId);
        } catch (IOException e) {
            log.error("Cloudinary upload failed for folder={}", folder, e);
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes the Cloudinary asset a previously-stored secure URL points to.
     * No-ops (with a log line) for null/blank/non-Cloudinary URLs — e.g. a
     * legacy local "/files/..." path from before this migration — and never
     * throws, since a stale image failing to clean up shouldn't fail the
     * caller's main operation (uploading/saving the replacement).
     */
    public void deleteByUrl(String secureUrl) {
        if (secureUrl == null || secureUrl.isBlank() || !secureUrl.contains(CLOUDINARY_HOST_MARKER)) {
            return;
        }
        String publicId = extractPublicId(secureUrl);
        if (publicId == null) {
            log.warn("Could not derive Cloudinary public_id from url={}, skipping delete", secureUrl);
            return;
        }
        deleteByPublicId(publicId);
    }

    /** Deletes every URL in previousUrls that's no longer present in currentUrls — for image-list fields (hotel/activity/escape point galleries) that get fully replaced on update. */
    public void deleteRemoved(List<String> previousUrls, List<String> currentUrls) {
        if (previousUrls == null || previousUrls.isEmpty()) {
            return;
        }
        Set<String> stillPresent = currentUrls == null ? Set.of() : new HashSet<>(currentUrls);
        for (String url : previousUrls) {
            if (!stillPresent.contains(url)) {
                deleteByUrl(url);
            }
        }
    }

    public void deleteByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted Cloudinary asset: publicId={}, result={}", publicId, result.get("result"));
        } catch (IOException e) {
            log.error("Failed to delete Cloudinary asset publicId={}", publicId, e);
        }
    }

    /** Cloudinary doesn't return public_id for values we merely read back out of an entity, so it's derived from the URL's own "/upload/[v<version>/]<public_id>.<ext>" structure. */
    static String extractPublicId(String secureUrl) {
        int uploadIdx = secureUrl.indexOf("/upload/");
        if (uploadIdx < 0) {
            return null;
        }
        String afterUpload = secureUrl.substring(uploadIdx + "/upload/".length());
        String[] segments = afterUpload.split("/");
        int start = (segments.length > 0 && segments[0].matches("v\\d+")) ? 1 : 0;
        if (start >= segments.length) {
            return null;
        }
        String pathWithExtension = String.join("/", Arrays.copyOfRange(segments, start, segments.length));
        int lastDot = pathWithExtension.lastIndexOf('.');
        return lastDot > 0 ? pathWithExtension.substring(0, lastDot) : pathWithExtension;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("File exceeds the 8MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Only JPEG, PNG, or WEBP images are allowed");
        }
    }
}
