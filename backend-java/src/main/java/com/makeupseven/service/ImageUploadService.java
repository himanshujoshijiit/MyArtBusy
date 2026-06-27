package com.makeupseven.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class ImageUploadService {

    private final Path uploadDir;

    public ImageUploadService(@Value("${makeupseven.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath();
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
        String ext = contentType.replace("image/", "").replace("jpeg", "jpg");
        String filename = UUID.randomUUID() + "." + ext;
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return "/uploads/" + filename;
    }
}
