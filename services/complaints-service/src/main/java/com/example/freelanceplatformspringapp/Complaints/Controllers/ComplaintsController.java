package com.example.freelanceplatformspringapp.Complaints.Controllers;

import com.example.freelanceplatformspringapp.Complaints.Entity.ClaimAttachment;
import com.example.freelanceplatformspringapp.Complaints.Entity.ClaimPriority;
import com.example.freelanceplatformspringapp.Complaints.Entity.Complaints;
import com.example.freelanceplatformspringapp.Complaints.Services.IComplaintsInterface;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/report")
@CrossOrigin(
    origins = "http://localhost:4200", 
    allowCredentials = "true",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*"
)
public class ComplaintsController {
    private final IComplaintsInterface ic;

    public ComplaintsController(IComplaintsInterface ic) {
        this.ic = ic;
    }

    @GetMapping("/retrieve-all-complaints")
    public List<Complaints> retrieveAllComplaints(@RequestParam("userId") Long userId) {
        // Only return visible complaints created by the given user
        return ic.retrieveComplaintsByUser(userId);
    }
    
    @GetMapping("/admin/retrieve-all-complaints")
    public List<Complaints> retrieveAllComplaintsForAdmin() {
        // Return all visible complaints regardless of user (for admin)
        return ic.retrieveAllVisibleComplaints();
    }

    @GetMapping("/retrieve-claim/{claim-id}")
    public Complaints retrieveClaimComplaint(@PathVariable("claim-id") Long claimId,
                                             @RequestParam("userId") Long userId) {
        // Only return if visible
        Optional<Complaints> claimOpt = ic.retrieveClaimForUser(claimId, userId);
        return claimOpt.orElse(null);
    }

    /**
     * Legacy JSON-based endpoint (without file upload).
     */
    @PostMapping(value = "/create-claim", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Complaints createClaim(@RequestBody Complaints c,
                                  @RequestParam("userId") Long userId) {
        c.setUserId(userId);
        return ic.addClaim(c);
    }

    /**
     * Multipart endpoint for creating a claim with optional file attachment.
     */
    @PostMapping(value = "/create-claim", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Complaints createClaimWithFile(@RequestParam("title") String title,
                                          @RequestParam("description") String description,
                                          @RequestParam("priority") ClaimPriority priority,
                                          @RequestParam("userId") Long userId,
                                          @RequestParam(value = "userEmail", required = false) String userEmail,
                                          @RequestPart(value = "file", required = false) MultipartFile file,
                                          @RequestParam(value = "imageUrl", required = false) String imageUrl,
                                          @RequestParam(value = "fileName", required = false) String fileName,
                                          @RequestParam(value = "fileType", required = false) String fileType,
                                          @RequestParam(value = "fileSize", required = false) Long fileSize) throws IOException {

        Complaints complaint = new Complaints();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setClaimPriority(priority);
        complaint.setUserId(userId);
        complaint.setCreatedAt(LocalDateTime.now());

        // Priority 1: Use ImgBB URL if provided (with metadata)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ClaimAttachment attachment = new ClaimAttachment();
            attachment.setFileName(fileName != null ? fileName : "ImgBB Image");
            attachment.setFileType(fileType != null ? fileType : "image");
            attachment.setFileSize(fileSize != null ? fileSize : 0L);
            attachment.setUploadedAt(LocalDateTime.now());
            attachment.setUploadedById(userId);
            attachment.setFileUrl(imageUrl); // Store ImgBB URL directly

            complaint.setClaimAttachment(attachment);
        }
        // Priority 2: Fallback to local file upload
        else if (file != null && !file.isEmpty()) {
            // Store file on disk
            Path uploadDir = Paths.get("uploads", "claims");
            Files.createDirectories(uploadDir);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String storedFileName = System.currentTimeMillis() + "_" + originalFilename;
            Path targetPath = uploadDir.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath);

            ClaimAttachment attachment = new ClaimAttachment();
            attachment.setFileName(originalFilename);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            attachment.setUploadedAt(LocalDateTime.now());
            attachment.setUploadedById(userId);
            // File will be served under /uploads/** via static resources
            attachment.setFileUrl("/uploads/claims/" + storedFileName);

            complaint.setClaimAttachment(attachment);
        }

        // Save complaint and send email with userEmail if provided
        return ic.addClaimWithEmail(complaint, userEmail);
    }

    @PutMapping("/update-claim")
    public Complaints updateClaim(@RequestBody Complaints c,
                                  @RequestParam("userId") Long userId) {
        // Ensure the complaint belongs to the user before updating
        if (c.getIdReclamation() == null) {
            return null;
        }
        Optional<Complaints> existing = ic.retrieveClaimForUser(c.getIdReclamation(), userId);
        if (existing.isEmpty()) {
            // Either not found or does not belong to this user
            return null;
        }
        c.setUserId(userId);
        return ic.updateComplaint(c);
    }
    
    @PutMapping("/admin/update-claim")
    public Complaints updateClaimByAdmin(@RequestBody Complaints c) {
        // Admin can update any complaint without userId check
        if (c.getIdReclamation() == null) {
            return null;
        }
        Optional<Complaints> existing = ic.retrieveClaim(c.getIdReclamation());
        if (existing.isEmpty()) {
            return null;
        }
        // Preserve the original userId
        c.setUserId(existing.get().getUserId());
        return ic.updateComplaint(c);
    }

    @DeleteMapping("/drop-claim/{claim-id}")
    void deleteClaim(@PathVariable("claim-id") Long claimId,
                     @RequestParam("userId") Long userId,
                     @RequestHeader(value = "X-Confirm-Delete", required = false) String confirmDelete) {
        // Only allow delete when explicitly confirmed by user action (prevents replay/refresh from triggering delete)
        if (!"true".equalsIgnoreCase(confirmDelete)) {
            return;
        }
        // Verify the claim belongs to the user and is visible before soft deleting
        Optional<Complaints> existing = ic.retrieveClaimForUser(claimId, userId);
        if (existing.isPresent()) {
            // Soft delete: set isVisible to false
            ic.deleteComplaint(claimId);
        }
    }
}
