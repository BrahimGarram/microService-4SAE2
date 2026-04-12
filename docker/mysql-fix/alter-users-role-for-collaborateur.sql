-- Corrige : Data truncated for column 'role' lors de l'inscription COLLABORATEUR
-- La table a souvent été créée avec un ENUM('USER','FREELANCER',...) sans COLLABORATEUR,
-- ou un VARCHAR trop court.
--
-- Exécuter UNE FOIS dans MySQL (Workbench, CLI, DBeaver) sur la base freelance_db :

USE freelance_db;

ALTER TABLE users MODIFY COLUMN role VARCHAR(32) NOT NULL;
