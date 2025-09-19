# Étape 1: Build de l'application
FROM maven:3.9.4-eclipse-temurin-17 AS build

WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Étape 2: Image de production
FROM eclipse-temurin:17-jre-alpine

# Installer Nginx
RUN apk add --no-cache nginx

# Créer les répertoires de logs
RUN mkdir -p /var/logs/crud

# Copier l'application Spring Boot
COPY --from=build /app/target/crud-api-*.jar /app/crud-api.jar

# Copier la configuration Nginx
COPY docker/nginx/nginx.conf /etc/nginx/nginx.conf

# Créer un script de démarrage
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'nginx &' >> /start.sh && \
    echo 'java -jar /app/crud-api.jar --spring.profiles.active=prod' >> /start.sh && \
    chmod +x /start.sh

# Exposer le port
EXPOSE 80

# Démarrer Nginx et l'application
CMD ["/start.sh"]