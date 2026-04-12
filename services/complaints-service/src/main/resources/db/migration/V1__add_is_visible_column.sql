-- Migration script to add isVisible column to complaints table
-- This ensures all existing complaints are marked as visible

-- Add the column with default value
ALTER TABLE complaints 
ADD COLUMN IF NOT EXISTS is_visible BOOLEAN DEFAULT TRUE;

-- Update any existing NULL values to TRUE
UPDATE complaints 
SET is_visible = TRUE 
WHERE is_visible IS NULL;

-- Make the column NOT NULL
ALTER TABLE complaints 
MODIFY COLUMN is_visible BOOLEAN NOT NULL DEFAULT TRUE;

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_complaints_user_visible 
ON complaints(user_id, is_visible);

-- Add index for cleanup queries (future use)
CREATE INDEX IF NOT EXISTS idx_complaints_visible_updated 
ON complaints(is_visible, updated_at);
