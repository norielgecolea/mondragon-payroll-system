# Build stage
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Serve with nginx
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/payroll-ui/browser/ /usr/share/nginx/html/
RUN chmod -R a+rX /usr/share/nginx/html
EXPOSE 80
