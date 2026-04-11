package com.example.freelanceplatformspringapp.Complaints.Services;

import com.example.freelanceplatformspringapp.Complaints.Entity.Complaints;

import java.util.List;
import java.util.Optional;

public interface IComplaintsInterface {

    Complaints addClaim(Complaints complaints);

    /**
     * Add a new claim with email notification
     */
    Complaints addClaimWithEmail(Complaints complaints, String userEmail);

    Optional<Complaints> retrieveClaim(Long id);

    /**
     * Retrieve all complaints (admin use).
     */
    List<Complaints> retrieveAllComplaints();
    
    /**
     * Retrieve all visible complaints (admin use).
     */
    List<Complaints> retrieveAllVisibleComplaints();

    /**
     * Retrieve all complaints created by a specific user.
     */
    List<Complaints> retrieveComplaintsByUser(Long userId);

    /**
     * Retrieve a single complaint only if it belongs to the given user.
     */
    Optional<Complaints> retrieveClaimForUser(Long id, Long userId);

    Complaints updateComplaint(Complaints complaints);

    void deleteComplaint(Long id);

}
