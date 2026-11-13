# ---------------------------------------------
# Dockerfile Multi-Stage - Frontend Angular 18
# Station Agil Management Portal
# ---------------------------------------------
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration production

# ---------------------------------------------
# Nginx Web Server Image
# ---------------------------------------------
FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
