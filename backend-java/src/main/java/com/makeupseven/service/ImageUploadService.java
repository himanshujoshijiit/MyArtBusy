package com.makeupseven.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    private final Path uploadDir;
    private final String cloudinaryCloudName;
    private final String cloudinaryUploadPreset;
    private final RestClient restClient = RestClient.create();

    public ImageUploadService(
            @Value("${makeupseven.upload-dir:uploads}") String uploadDir,
            @Value("${makeupseven.cloudinary.cloud-name:}") String cloudinaryCloudName,
            @Value("${makeupseven.cloudinary.upload-preset:}") String cloudinaryUploadPreset) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath();
        this.cloudinaryCloudName = cloudinaryCloudName;
        this.cloudinaryUploadPreset = cloudinaryUploadPreset;
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            log.warn("Could not create upload dir: {}", e.getMessage());
        }
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new RuntimeException("Empty file");
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File too large (max 5MB)");
        }

        if (cloudinaryCloudName != null && !cloudinaryCloudName.isBlank()
                && cloudinaryUploadPreset != null && !cloudinaryUploadPreset.isBlank()) {
            return uploadToCloudinary(file);
        }
        return uploadToLocal(file, contentType);
    }

    private String uploadToCloudinary(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg";
            }
        });
        body.add("upload_preset", cloudinaryUploadPreset);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri("https://api.cloudinary.com/v1_1/" + cloudinaryCloudName + "/image/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("secure_url")) {
            throw new RuntimeException("Cloudinary upload failed");
        }
        return (String) response.get("secure_url");
    }

    private String uploadToLocal(MultipartFile file, String contentType) throws IOException {
        String ext = contentType.replace("image/", "").replace("jpeg", "jpg");
        String filename = UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return "/uploads/" + filename;
    }
}
