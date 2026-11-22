# ==========================================
# FASE 1: BUILD (Compilazione Angular)
# ==========================================
FROM node:20-alpine AS build
WORKDIR /app

# Copia solo il package.json per sfruttare la cache di Docker
COPY package.json ./
RUN npm install

# Copia tutto il codice sorgente e compila per la produzione
COPY . .
RUN npm run build

# ==========================================
# FASE 2: RUNTIME (Server Web Nginx)
# ==========================================
FROM nginx:alpine

# Sostituisce la configurazione predefinita di Nginx con la nostra
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia i file statici compilati da Angular nella directory di Nginx
COPY --from=build /app/dist/fullstack-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
