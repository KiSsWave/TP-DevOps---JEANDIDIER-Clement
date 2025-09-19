# API CRUD - TD DevOps

API REST conteneurisée développée avec Spring Boot, MariaDB et Nginx pour le TD DevOps CI/CD et Monitoring.

## Prérequis

- Docker et Docker Compose installés
- Ports 8080 et 3307 libres sur votre machine

### 1. Créer le fichier de configuration

Créez un fichier `.env` à la racine du projet avec le contenu suivant :

```env
# Configuration base de données
DB_URL=jdbc:mariadb://db:3306/cruddb
DB_USERNAME=root
DB_PASSWORD=root

# Configuration application
SERVER_PORT=8080
LOG_LEVEL=INFO
SHOW_SQL=false

# Configuration JPA et Flyway
JPA_DDL_AUTO=create-drop
FLYWAY_ENABLED=false

# Configuration logs
LOG_FILE_PATH=/var/logs/crud/app.log
```

### 2. Démarrer l'application

```bash
docker-compose up --build
```

L'application sera accessible sur : http://localhost:8080

### 3. Vérifier le démarrage

```bash
# Health check
curl http://localhost:8080/health

# Ou dans le navigateur
# http://localhost:8080/health
```

