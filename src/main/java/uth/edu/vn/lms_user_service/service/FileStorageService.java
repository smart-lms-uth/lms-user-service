package uth.edu.vn.lms_user_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import uth.edu.vn.lms_user_service.exception.ApiException;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            Files.createDirectories(uploadPath.resolve("avatars"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeAvatar(MultipartFile file, Long userId) {
        validateFile(file);
        
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        
        try {
            Path avatarPath = uploadPath.resolve("avatars");
            
            // Delete old avatars for this user
            Files.list(avatarPath)
                .filter(path -> path.getFileName().toString().startsWith("avatar_" + userId + "_"))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {}
                });
            
            Path targetLocation = avatarPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return baseUrl + "/uploads/avatars/" + filename;
        } catch (IOException e) {
            throw ApiException.internalError("Failed to store avatar file");
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        validateFile(file);
        
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;
        
        try {
            Path targetDir = uploadPath.resolve(subDirectory);
            Files.createDirectories(targetDir);
            
            Path targetLocation = targetDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return baseUrl + "/uploads/" + subDirectory + "/" + filename;
        } catch (IOException e) {
            throw ApiException.internalError("Failed to store file");
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        
        try {
            String relativePath = fileUrl.replace(baseUrl + "/uploads/", "");
            Path filePath = uploadPath.resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Ignore deletion errors
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw ApiException.badRequest("File size exceeds maximum allowed size (5MB)");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw ApiException.badRequest("Invalid file type. Allowed: JPEG, PNG, GIF, WebP");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
