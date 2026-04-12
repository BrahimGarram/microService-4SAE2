-- Colonne role assez large pour COLLABORATEUR (évite Data truncated / ENUM obsolète).
-- Ne s'exécute qu'au premier démarrage du volume MySQL ; si la table users n'existe pas encore (JPA), la procédure ne fait rien.
-- Les volumes déjà existants : correctif au démarrage du user-service (UsersRoleColumnCompatibility).

USE freelance_db;

DELIMITER $$

DROP PROCEDURE IF EXISTS freelance_fix_users_role$$

CREATE PROCEDURE freelance_fix_users_role()
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users'
    ) THEN
        ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL;
    END IF;
END$$

DELIMITER ;

CALL freelance_fix_users_role();

DROP PROCEDURE IF EXISTS freelance_fix_users_role;
