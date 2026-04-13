# Migrations PostgreSQL local (port 5432), depuis services/collaborationService :
#   .\scripts\migrate-local.ps1
#
# Prérequis : utilisateur collab + base collaboration_db (voir docker/postgres-local-collaboration-init.sql)
# et extension PHP pdo_pgsql activée dans php.ini (décommenter extension=pdo_pgsql).

$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot\..

$env:DATABASE_URL = 'postgresql://collab:collab@127.0.0.1:5432/collaboration_db?serverVersion=16&charset=utf8'
php bin/console doctrine:migrations:migrate --no-interaction
