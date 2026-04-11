package com.example.freelanceplatformspringapp.Complaints.Repository;

import com.example.freelanceplatformspringapp.Complaints.Entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    
    // Find all penalties for a user
    List<Penalty> findByUserId(Long userId);
    
    // Find active penalties for a user
    List<Penalty> findByUserIdAndIsActiveTrue(Long userId);
    
    // Find penalties by complaint
    List<Penalty> findByComplaintId(Long complaintId);
    
    // Count active penalties for a user
    long countByUserIdAndIsActiveTrue(Long userId);
}
