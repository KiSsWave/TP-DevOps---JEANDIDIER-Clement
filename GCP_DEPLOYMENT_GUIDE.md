# Guide de Déploiement sur Google Cloud Run

Ce guide explique comment configurer le déploiement automatique de votre API CRUD sur Google Cloud Run via GitHub Actions.

## Architecture du Déploiement

```
GitHub Actions (CI/CD)
  ↓
1. Build Images Docker
   ├── CRUD API (Spring Boot + Nginx)
   └── Fluent Bit (Collecteur de logs)
  ↓
2. Push sur Docker Hub
  ↓
3. Exécution des Migrations (Flyway + Cloud SQL Proxy)
  ↓
4. Déploiement sur Cloud Run
  ↓
5. Service accessible publiquement
```

---

## Prérequis

### 1. Compte Google Cloud Platform

- Créez un compte sur https://console.cloud.google.com
- Créez un nouveau projet GCP (notez l'ID du projet)
- Activez la facturation sur le projet

### 2. APIs Google Cloud à activer

Allez dans **APIs & Services** → **Library** et activez :

- **Cloud Run API**
- **Cloud SQL Admin API**
- **Secret Manager API** (optionnel, pour stocker les secrets)
- **Container Registry API** (si vous utilisez GCR au lieu de Docker Hub)

Ou via gcloud CLI :
```bash
gcloud services enable run.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable secretmanager.googleapis.com
```

---

## Configuration Google Cloud Platform

### Étape 1 : Créer une instance Cloud SQL (MariaDB)

1. **Allez dans Cloud SQL** : https://console.cloud.google.com/sql

2. **Cliquez sur "Create Instance"**

3. **Choisissez MySQL** (compatible MariaDB)

4. **Configurez l'instance** :
   - **Instance ID** : `crud-mariadb` (ou autre nom)
   - **Password** : Définissez un mot de passe root sécurisé
   - **Database version** : MySQL 8.0 ou MariaDB 10.x
   - **Region** : `europe-west1` (même région que Cloud Run)
   - **Zone** : Laissez par défaut
   - **Machine type** : `db-f1-micro` (pour dev/test) ou `db-n1-standard-1` (prod)
   - **Storage** : 10 GB (augmentez si nécessaire)

5. **Cliquez sur "Create"** (cela peut prendre 5-10 minutes)

6. **Notez le "Connection name"** (format: `project-id:region:instance-name`)

7. **Créez une base de données** :
   - Allez dans l'onglet **Databases**
   - Cliquez sur **Create database**
   - **Name** : `cruddb`
   - Cliquez sur **Create**

8. **Créez un utilisateur** (optionnel, recommandé pour la sécurité) :
   - Allez dans l'onglet **Users**
   - Cliquez sur **Add user account**
   - **Username** : `crud_user`
   - **Password** : Définissez un mot de passe sécurisé
   - **Host** : `%` (pour autoriser toutes les connexions)

### Étape 2 : Créer un Service Account

1. **Allez dans IAM & Admin** → **Service Accounts** : https://console.cloud.google.com/iam-admin/serviceaccounts

2. **Cliquez sur "Create Service Account"**

3. **Configurez le compte** :
   - **Service account name** : `github-actions-deployer`
   - **Service account ID** : `github-actions-deployer` (généré automatiquement)
   - **Description** : `Service account for GitHub Actions CI/CD`

4. **Cliquez sur "Create and Continue"**

5. **Assignez les rôles** (sélectionnez les rôles suivants) :
   - `Cloud Run Admin` - Pour déployer sur Cloud Run
   - `Cloud SQL Client` - Pour se connecter à Cloud SQL
   - `Service Account User` - Pour utiliser le service account
   - `Storage Admin` (optionnel) - Si vous utilisez Cloud Storage

6. **Cliquez sur "Continue"** puis **"Done"**

7. **Créez une clé JSON** :
   - Cliquez sur le service account que vous venez de créer
   - Allez dans l'onglet **Keys**
   - Cliquez sur **Add Key** → **Create new key**
   - **Key type** : JSON
   - Cliquez sur **Create**
   - **SAUVEGARDEZ LE FICHIER JSON** (vous ne pourrez plus le télécharger!)

---

## Configuration des Secrets GitHub

Allez sur votre repository GitHub → **Settings** → **Secrets and variables** → **Actions**

### Secrets Docker Hub (déjà configurés)

- ✅ `DOCKERHUB_USERNAME` : Votre nom d'utilisateur Docker Hub
- ✅ `DOCKERHUB_TOKEN` : Votre token Docker Hub (Read & Write)

### Nouveaux Secrets GCP

| Secret | Description | Exemple |
|--------|-------------|---------|
| `GCP_SA_KEY` | Contenu du fichier JSON du service account | `{"type": "service_account", ...}` |
| `GCP_PROJECT_ID` | ID de votre projet GCP | `my-project-123456` |
| `CLOUDSQL_INSTANCE_NAME` | Nom de l'instance Cloud SQL (SEULEMENT le nom) | `crud-mariadb` |
| `DB_NAME` | Nom de la base de données | `cruddb` |
| `DB_USER` | Utilisateur de la base de données | `root` ou `crud_user` |
| `DB_PASSWORD` | Mot de passe de la base de données | `votre_mot_de_passe_securise` |
| `LOKI_URL` | URL de votre instance Loki (pour les logs) | `http://35.205.158.67:3100` |

### Comment ajouter les secrets

Pour chaque secret :

1. Cliquez sur **"New repository secret"**
2. **Name** : Nom du secret (voir tableau ci-dessus)
3. **Secret** : Valeur du secret
4. Cliquez sur **"Add secret"**

**Important pour `GCP_SA_KEY`** :
- Ouvrez le fichier JSON du service account avec un éditeur de texte
- **Copiez TOUT le contenu** (de `{` à `}`)
- Collez-le directement dans le champ Secret

---

## Vérification de la Configuration

### Checklist avant le déploiement

- [ ] Projet GCP créé et facturation activée
- [ ] APIs Cloud Run et Cloud SQL activées
- [ ] Instance Cloud SQL créée et accessible
- [ ] Base de données `cruddb` créée
- [ ] Service Account créé avec les bons rôles
- [ ] Fichier JSON du service account téléchargé
- [ ] Tous les secrets GitHub configurés (9 secrets au total)
- [ ] Docker Hub configuré avec Read & Write

---

## Déploiement

### Déclencher le workflow

Une fois tous les secrets configurés :

```bash
# 1. Commitez vos changements
git add .
git commit -m "feat: add Cloud Run deployment"
git push origin master

# 2. Créez un tag de version
git tag v2.0.0

# 3. Poussez le tag pour déclencher le workflow
git push origin v2.0.0
```

### Étapes du workflow

Le workflow GitHub Actions va automatiquement :

1. ✅ **Récupérer le code** depuis GitHub
2. ✅ **Extraire la version** : `v2.0.0` → `2.0.0`
3. ✅ **Se connecter à Docker Hub**
4. ✅ **Build et push de l'image CRUD API** (`crud-api:2.0.0` et `latest`)
5. ✅ **Build et push de l'image Fluent Bit** (`crud-api-fluentbit:2.0.0` et `latest`)
6. ✅ **S'authentifier sur Google Cloud**
7. ✅ **Build de l'image de migrations**
8. ✅ **Exécuter les migrations Flyway** via Cloud SQL Proxy
9. ✅ **Mettre à jour le fichier Cloud Run** avec les valeurs réelles
10. ✅ **Déployer sur Cloud Run**
11. ✅ **Configurer les permissions publiques**
12. ✅ **Récupérer l'URL du service**

---

## Vérification du Déploiement

### 1. GitHub Actions

- Allez dans **Actions** sur GitHub
- Vérifiez que le workflow "Build and Deploy to Cloud Run" est vert ✅
- Consultez les logs pour voir l'URL du service déployé

### 2. Google Cloud Console

1. **Cloud Run** : https://console.cloud.google.com/run
   - Vous devriez voir votre service `crud-api`
   - Cliquez dessus pour voir les détails
   - Copiez l'URL du service (ex: `https://crud-api-xyz123-ew.a.run.app`)

2. **Cloud SQL** :
   - Vérifiez que les connexions sont actives dans l'onglet **Monitoring**

### 3. Tester l'API

```bash
# Récupérez l'URL depuis Cloud Run Console ou les logs GitHub Actions
export SERVICE_URL="https://crud-api-xyz123-ew.a.run.app"

# Test du health check
curl $SERVICE_URL/actuator/health

# Test de l'API
curl $SERVICE_URL/api/users
```

---

## Structure des Fichiers

```
crud-api/
├── .github/
│   └── workflows/
│       └── docker-build-push.yml        # Workflow CI/CD complet
├── crud-api/
│   ├── Dockerfile                       # Image principale (Spring Boot + Nginx)
│   ├── Dockerfile.fluentbit             # Image Fluent Bit
│   ├── Dockerfile.migrations            # Image pour les migrations
│   ├── cloud-run-service.yaml           # Configuration Cloud Run
│   ├── fluent-bit.conf                  # Config Fluent Bit
│   ├── parsers.conf                     # Parsers Fluent Bit
│   ├── pom.xml                          # Config Maven
│   └── src/
│       └── main/
│           └── resources/
│               └── db/migration/        # Scripts Flyway
│                   ├── V1__Create_users_table.sql
│                   └── V2__Insert_sample_data.sql
├── CI_CD_GUIDE.md                       # Guide Docker Hub (précédent)
└── GCP_DEPLOYMENT_GUIDE.md              # Ce guide
```

---

## Troubleshooting

### Erreur : "Permission denied" lors du déploiement

**Solution** : Vérifiez que le service account a les rôles `Cloud Run Admin` et `Service Account User`

### Erreur : "Cloud SQL connection failed"

**Solution** :
1. Vérifiez que l'instance Cloud SQL est bien démarrée
2. Vérifiez le format de `INSTANCE_CONNECTION_NAME` : `project-id:region:instance-name`
3. Vérifiez que le service account a le rôle `Cloud SQL Client`

### Erreur : "Invalid credentials"

**Solution** : Vérifiez que le secret `GCP_SA_KEY` contient bien TOUT le contenu du fichier JSON (de `{` à `}`)

### Les migrations échouent

**Solution** :
1. Vérifiez les credentials de la base de données (`DB_USER`, `DB_PASSWORD`)
2. Vérifiez que la base `cruddb` existe bien
3. Consultez les logs du step "Run database migrations" dans GitHub Actions

### L'image ne se lance pas sur Cloud Run

**Solution** :
1. Vérifiez que l'image existe bien sur Docker Hub
2. Vérifiez que le port exposé est bien 80 (dans le Dockerfile)
3. Consultez les logs Cloud Run : Console → Cloud Run → Service → Logs

---

## Coûts estimés (Google Cloud)

### Calcul approximatif pour un projet de test/dev

- **Cloud Run** : Gratuit jusqu'à 2 millions de requêtes/mois
- **Cloud SQL (db-f1-micro)** : ~7-10€/mois
- **Stockage Cloud SQL (10 GB)** : ~1€/mois
- **Réseau (egress)** : Variable selon l'usage

**Total estimé** : 10-15€/mois pour un petit projet

**Optimisations** :
- Utilisez Cloud Run avec scale-to-zero (pas de coût quand inactif)
- Arrêtez Cloud SQL quand non utilisé (dev/test)
- Configurez des budgets et alertes dans GCP

---

## Prochaines étapes

- Ajouter des tests automatiques avant le déploiement
- Configurer un environnement de staging
- Ajouter des alertes et monitoring (Cloud Monitoring)
- Configurer un nom de domaine personnalisé
- Ajouter HTTPS avec certificat SSL géré par Google
- Implémenter le déploiement Blue/Green ou Canary

---

## Support

En cas de problème :
1. Consultez les logs GitHub Actions
2. Consultez les logs Cloud Run : https://console.cloud.google.com/run
3. Consultez les logs Cloud SQL
4. Vérifiez que tous les secrets sont correctement configurés

**Logs utiles** :
```bash
# Logs Cloud Run
gcloud run services logs read crud-api --region=europe-west1

# Décrire le service
gcloud run services describe crud-api --region=europe-west1
```
