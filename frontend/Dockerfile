# Stage 1: Build Angular application
FROM node:22-alpine AS build
WORKDIR /app/frontend

# Copy package files and install dependencies
COPY frontend/package*.json ./
RUN npm ci

# Copy full frontend workspace and build
COPY frontend/ ./
RUN npm run build -- --configuration production

# Stage 2: Serve application with Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Clean default Nginx content
RUN rm -rf ./*

# Copy Angular production build artifacts to Nginx web root
COPY --from=build /app/frontend/dist/frontend/browser ./

# Copy custom Nginx configuration
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
