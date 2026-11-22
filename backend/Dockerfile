# ==========================================
# FASE 1: BUILD (Compilazione Maven in container)
# ==========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia solo il pom.xml per scaricare le dipendenze in cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia i sorgenti e compila il jar saltando i test
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# FASE 2: RUNTIME (Immagine finale leggera)
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utente non-root per sicurezza (best practice container)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copia il jar generato dalla fase di build
COPY --from=build /app/target/fullstack-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
