#!/bin/sh
set -e

# Clear and recreate cache directory with proper permissions
rm -rf var/cache/prod 2>/dev/null || true
mkdir -p var/cache/prod var/log var/collaboration_uploads
chmod -R 777 var
chown -R www-data:www-data var public

# Warm up cache as www-data user
su -s /bin/sh www-data -c "php bin/console cache:warmup --env=prod --no-debug" || true

php bin/console doctrine:migrations:migrate --no-interaction --allow-no-migration
exec apache2-foreground
