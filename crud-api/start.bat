@echo off
echo 🚀 Démarrage de l'application CRUD API...

REM Créer le répertoire de logs sur l'hôte
mkdir logs 2>nul

REM Construire et démarrer les services
docker-compose up --build -d

echo ✅ Application démarrée!
echo 📊 API disponible sur: http://localhost:8080
echo 🏥 Health check: http://localhost:8080/health
echo 📋 Logs disponibles dans: ./logs/

REM Afficher les logs
docker-compose logs -f