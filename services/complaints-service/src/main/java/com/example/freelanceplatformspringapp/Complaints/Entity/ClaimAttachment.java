package com.example.freelanceplatformspringapp.Complaints.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClaimAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAttachment;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private Long claimId;
    private Long uploadedById;
}