-- Kanban list table for project-service (formerly "tasks")
-- Run this script in your MySQL database

CREATE TABLE IF NOT EXISTS kanbanlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TO_DO',
    project_id BIGINT NOT NULL,
    assigned_to BIGINT,
    created_by BIGINT NOT NULL,
    due_date DATETIME,
    completed_at DATETIME,
    priority INT DEFAULT 0,
    order_index INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_assigned_to (assigned_to),
    INDEX idx_status (status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
