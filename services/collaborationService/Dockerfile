# Aligné sur composer.lock (phpunit 12.x et dépendances → PHP >= 8.3)
FROM php:8.3-apache

RUN apt-get update && apt-get install -y --no-install-recommends \
    libpq-dev \
    unzip \
    git \
    && docker-php-ext-install pdo_pgsql \
    && a2enmod rewrite headers \
    && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

ENV APACHE_DOCUMENT_ROOT=/var/www/html/public
RUN sed -ri -e 's!/var/www/html!${APACHE_DOCUMENT_ROOT}!g' /etc/apache2/sites-available/*.conf /etc/apache2/apache2.conf

WORKDIR /var/www/html

COPY . .

# Évite que les auto-scripts (cache:clear) tournent en « dev » et exigent debug-bundle absent avec --no-dev
ENV APP_ENV=prod
ENV APP_DEBUG=0
ENV MESSENGER_TRANSPORT_DSN=sync://

# Create minimal .env file for build (runtime env vars will override)
RUN echo "APP_ENV=prod" > .env \
    && echo "APP_SECRET=build_secret_will_be_overridden" >> .env \
    && echo "DATABASE_URL=postgresql://dummy:dummy@localhost:5432/dummy" >> .env

# Install dependencies without running auto-scripts (cache:clear needs DB)
RUN composer install --no-dev --optimize-autoloader --no-interaction --no-scripts \
    && mkdir -p var/collaboration_uploads var/cache var/log \
    && chmod -R 777 var \
    && chown -R www-data:www-data var public

RUN printf 'upload_max_filesize=64M\npost_max_size=64M\n' > /usr/local/etc/php/conf.d/uploads.ini

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
# Windows CRLF dans le script → « no such file or directory » au exec ; normaliser en LF
RUN sed -i 's/\r$//' /usr/local/bin/docker-entrypoint.sh \
    && chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 80

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
