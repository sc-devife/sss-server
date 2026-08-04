package com.sss.app.service.files;

/** Result of a successful Cloudinary upload — secureUrl is what gets persisted/rendered, publicId is what's needed to delete the asset later. */
public record CloudinaryUploadResult(String secureUrl, String publicId) {
}
