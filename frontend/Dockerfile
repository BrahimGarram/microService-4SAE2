# Build Angular (config docker → gateway + Keycloak via localhost)
# NODE_OPTIONS + moins de workers : limite les pics RAM (sinon OOM → process tué → EOF côté Docker Desktop).
FROM node:20-alpine AS build
WORKDIR /app
ENV NODE_OPTIONS="--max-old-space-size=6144"
ENV NG_BUILD_MAX_WORKERS=2
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=docker

FROM nginx:alpine
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/freelance-platform /usr/share/nginx/html
EXPOSE 80
