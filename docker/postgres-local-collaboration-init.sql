-- À exécuter UNE FOIS en superutilisateur PostgreSQL local (port 5432), ex. :
--   psql -U postgres -h 127.0.0.1 -p 5432 -f docker/postgres-local-collaboration-init.sql
--
-- Ensuite : dans services/collaborationService, lancer
--   php bin/console doctrine:migrations:migrate --no-interaction

CREATE USER collab WITH PASSWORD 'collab';
CREATE DATABASE collaboration_db OWNER collab;
GRANT ALL PRIVILEGES ON DATABASE collaboration_db TO collab;
