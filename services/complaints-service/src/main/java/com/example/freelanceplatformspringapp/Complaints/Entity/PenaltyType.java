package com.example.freelanceplatformspringapp.Complaints.Entity;

public enum PenaltyType {
    WARNING,              // First offense - warning only
    ACCOUNT_RESTRICTION,  // Restrict certain features
    TEMPORARY_SUSPENSION, // Suspend account temporarily
    PERMANENT_BAN,        // Permanent account ban
    FINE                  // Monetary penalty
}
