-- Fix: allow ENTERPRISE role in users.role (évite "Data truncated for column 'role'")
-- Exécuter une fois sur la base freelance_db si la colonne est ENUM ou VARCHAR trop court.

USE freelance_db;

-- Si la colonne est un ENUM sans ENTERPRISE, la passer en VARCHAR(20)
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL;
