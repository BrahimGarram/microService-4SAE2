-- Migration script to add automation fields to complaints table
-- Run this if you're not using ddl-auto=update

-- Add category field
ALTER TABLE complaints 
ADD COLUMN category VARCHAR(50) DEFAULT 'OTHER' AFTER claim_priority;

-- Add assigned admin field
ALTER TABLE complaints 
ADD COLUMN assigned_to_admin_id BIGINT NULL AFTER category;

-- Add last reminder sent timestamp
ALTER TABLE complaints 
ADD COLUMN last_reminder_sent_at DATETIME NULL AFTER resolved_at;

-- Add indexes for better performance
CREATE INDEX idx_complaints_status ON complaints(claim_status);
CREATE INDEX idx_complaints_category ON complaints(category);
CREATE INDEX idx_complaints_assigned_admin ON complaints(assigned_to_admin_id);
CREATE INDEX idx_complaints_created_at ON complaints(created_at);
CREATE INDEX idx_complaints_updated_at ON complaints(updated_at);

-- Update existing complaints to have default category
UPDATE complaints SET category = 'OTHER' WHERE category IS NULL;
