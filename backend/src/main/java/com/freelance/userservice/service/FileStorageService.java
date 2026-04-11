package com.freelance.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * File Storage Service
 * 
 * Handles file uploads, especially profile pictures.
 * Stores files locally in the uploads directory.
 */
@Service
@Slf4j
public class FileStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base.url:http://localhost:8081}")
    private String baseUrl;
    
    /**
     * Upload and save a profile picture
     * 
     * @param file MultipartFile to upload
     * @param userId User ID for organizing files
     * @return URL to access the uploaded file
     */
    public String uploadProfilePicture(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, "profiles", userId.toString());
        Files.createDirectories(uploadPath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Generate URL
        String fileUrl = baseUrl + "/uploads/profiles/" + userId + "/" + filename;
        
        log.info("Profile picture uploaded for user {}: {}", userId, fileUrl);
        
        return fileUrl;
    }
    
    /**
     * Delete a profile picture
     * 
     * @param fileUrl URL of the file to delete
     * @param userId User ID
     */
    public void deleteProfilePicture(String fileUrl, Long userId) {
        try {
            if (fileUrl == null || fileUrl.isBlank()) {
                return;
            }
            
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir, "profiles", userId.toString(), filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Profile picture deleted for user {}: {}", userId, filename);
            }
        } catch (IOException e) {
            log.error("Error deleting profile picture for user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Resize image (placeholder - would require image processing library like Thumbnailator)
     * For now, we'll just store the original image
     */
    private void resizeImage(Path imagePath, int width, int height) {
        // TODO: Implement image resizing using Thumbnailator or similar library
        // This would create thumbnails for profile pictures
    }
}
