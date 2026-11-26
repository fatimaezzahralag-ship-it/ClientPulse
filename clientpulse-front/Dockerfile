# =============================================================================
# Frontend ClientPulse : build Angular puis service par nginx.
# nginx sert l'application et relaie /api vers le backend : même origine, donc
# cookie de session et jeton CSRF fonctionnent sans configuration CORS.
# =============================================================================
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration production

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/clientpulse-front/browser /usr/share/nginx/html
EXPOSE 80
