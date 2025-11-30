# Guide CI/CD - Build et Push Docker Image

## Vue d'ensemble

Ce workflow GitHub Actions automatise le processus de construction et de publication de l'image Docker de l'API CRUD sur Docker Hub.

## Étapes du workflow

### 1. Récupération du code ✅
Le code source est téléchargé depuis GitHub automatiquement.

### 2. Extraction de la version
Le numéro de version est extrait du tag Git (exemple: `v1.0.0` → `1.0.0`)

### 3. Connexion à Docker Hub
Authentification automatique sur Docker Hub avec les identifiants stockés dans les secrets GitHub.

### 4. Build et Push de l'image
- Construction de l'image Docker de l'application CRUD (Spring Boot + Nginx)
- Publication avec le tag de version spécifique (ex: `1.0.0`)
- Publication avec le tag `latest`
- Utilisation du cache pour accélérer les builds suivants

## Configuration requise

### Secrets GitHub à configurer

Vous devez ajouter ces secrets dans votre repository GitHub :

1. **DOCKERHUB_USERNAME** : Votre nom d'utilisateur Docker Hub
2. **DOCKERHUB_TOKEN** : Votre token d'accès Docker Hub (pas votre mot de passe!)

#### Comment créer un token Docker Hub :

1. Connectez-vous sur https://hub.docker.com
2. Allez dans **Account Settings** → **Security**
3. Cliquez sur **New Access Token**
4. Donnez un nom (ex: `github-actions-crud-api`)
5. Copiez le token (vous ne pourrez plus le voir après!)

#### Comment ajouter les secrets dans GitHub :

1. Allez sur votre repository GitHub
2. **Settings** → **Secrets and variables** → **Actions**
3. Cliquez sur **New repository secret**
4. Ajoutez `DOCKERHUB_USERNAME` avec votre nom d'utilisateur
5. Ajoutez `DOCKERHUB_TOKEN` avec votre token

## Utilisation

### Déclencher le workflow

Le workflow se déclenche automatiquement quand vous créez un **tag de version** :

```bash
# Créer un tag de version
git tag v1.0.0

# Pousser le tag sur GitHub
git push origin v1.0.0
```

### Format des tags

- Format requis : `v*.*.*` (exemple: `v1.0.0`, `v2.1.3`, `v1.0.0-beta`)
- Le 'v' au début est obligatoire pour déclencher le workflow
- Suit le versioning sémantique (majeur.mineur.patch)

### Résultat

Après l'exécution réussie, deux images seront disponibles sur Docker Hub :

```
votreusername/crud-api:1.0.0
votreusername/crud-api:latest
```

## Exemples de commandes

### Créer une nouvelle version

```bash
# 1. Commit vos changements
git add .
git commit -m "feat: nouvelle fonctionnalité"

# 2. Créer un tag de version
git tag v1.1.0

# 3. Pousser le code et le tag
git push origin master
git push origin v1.1.0

# 4. Le workflow GitHub Actions se lance automatiquement
```

### Utiliser l'image Docker

```bash
# Tirer la dernière version
docker pull votreusername/crud-api:latest

# Tirer une version spécifique
docker pull votreusername/crud-api:1.0.0

# Lancer le conteneur
docker run -d -p 8080:80 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://db:3306/cruddb \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  votreusername/crud-api:latest
```

## Vérification

### Voir les workflows en cours

1. Allez sur votre repository GitHub
2. Cliquez sur l'onglet **Actions**
3. Vous verrez tous les workflows qui s'exécutent ou qui sont terminés

### Vérifier sur Docker Hub

1. Connectez-vous sur https://hub.docker.com
2. Allez dans **Repositories**
3. Vous devriez voir `crud-api` avec les différents tags

## Troubleshooting

### Erreur d'authentification Docker Hub
- Vérifiez que `DOCKERHUB_USERNAME` et `DOCKERHUB_TOKEN` sont bien configurés
- Vérifiez que le token n'a pas expiré
- Créez un nouveau token si nécessaire

### Le workflow ne se déclenche pas
- Vérifiez que le tag commence bien par 'v' (ex: `v1.0.0`)
- Vérifiez que vous avez bien poussé le tag : `git push origin v1.0.0`

### Erreur de build Docker
- Vérifiez que le Dockerfile est valide
- Vérifiez les dépendances Maven dans le `pom.xml`
- Consultez les logs du workflow dans l'onglet Actions

## Optimisations

Le workflow utilise plusieurs optimisations :

- **Docker Buildx** : Pour des builds plus rapides et multi-plateforme
- **Layer caching** : Réutilise les layers précédents pour accélérer les builds
- **Cache Registry** : Stocke le cache dans le registry Docker pour partager entre les builds

## Prochaines étapes possibles

- Ajouter des tests automatiques avant le build
- Déployer automatiquement sur un serveur après le push
- Ajouter des notifications (Slack, Discord, Email)
- Scanner l'image pour les vulnérabilités de sécurité
