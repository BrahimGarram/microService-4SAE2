package com.microservices.contract.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idContract;
    
    @Column(nullable = false, length = 100)
    private String contractTitle;
    
    @Column(nullable = false, length = 100)
    private String freelancerName;
    
    @Column(nullable = false, length = 100)
    private String freelancerEmail;
    
    @Column(nullable = false, length = 100)
    private String clientName;
    
    @Column(nullable = false, length = 100)
    private String clientEmail;
    
    @Column(columnDefinition = "TEXT")
    private String projectDescription;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal contractAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;
    
    @Column(length = 50)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private ContractType contractType;
    
    @Column(columnDefinition = "TEXT")
    private String deliverables;
    
    @Column(columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @Column
    private Integer paymentTermsDays;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @Column(length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currency == null) {
            currency = "USD";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
