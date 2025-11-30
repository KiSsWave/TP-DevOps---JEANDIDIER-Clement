# Configuration Fluent Bit + Loki + Grafana + Discord

Ce guide vous explique comment configurer Fluent Bit pour envoyer vos logs vers Loki sur GCP et configurer des alertes Discord.

## Prérequis

1. Instance Loki déployée sur GCP
2. Grafana connecté à Loki
3. Webhook Discord configuré

## Étape 1: Configuration des variables d'environnement

Copiez le fichier `.env.example` vers `.env` et configurez les variables Loki :

```bash
cp .env.example .env
```

Éditez `.env` avec vos informations Loki GCP :

```bash
# Configuration Loki (GCP)
# Pour HTTPS (GCP): utilisez l'URI complète
LOKI_HOST=https://your-loki-instance.europe-west1.run.app
LOKI_PORT=443
LOKI_USER=your_username
LOKI_PASSWORD=your_password
ENVIRONMENT=production
HOSTNAME=crud-app-prod

# Pour HTTP local:
# LOKI_HOST=localhost
# LOKI_PORT=3100
```

**Notes importantes :**
- `LOKI_HOST` : Pour HTTPS, incluez `https://` (ex: `https://loki.example.com`). Pour HTTP local, juste `localhost`
- `LOKI_PORT` : 443 si HTTPS, 3100 si HTTP
- La configuration Fluent Bit 3.0 utilise maintenant `Uri` au lieu de `Host+Port+Tls`
- Si votre Loki GCP n'a pas d'authentification, laissez `LOKI_USER` et `LOKI_PASSWORD` vides

## Étape 2: Démarrage de Fluent Bit

Redémarrez vos services Docker avec Fluent Bit :

```bash
docker-compose down
docker-compose up -d
```

Vérifiez que Fluent Bit fonctionne :

```bash
docker logs crud-fluent-bit -f
```

Vous devriez voir :
```
[info] [output:loki:loki.0] loki.example.com:443, HTTP status=204
```

Si vous voyez des erreurs, vérifiez :
- Les credentials Loki sont corrects
- Le hostname Loki est accessible
- Le port est le bon (443 pour HTTPS)

## Étape 3: Vérifier les logs dans Grafana Explorer

1. Connectez-vous à Grafana
2. Allez dans **Explore** (icône boussole à gauche)
3. Sélectionnez votre datasource **Loki**
4. Utilisez cette requête LogQL :

```logql
{job="crud-api"}
```

Ou pour filtrer par niveau :

```logql
{job="crud-api"} |= "ERROR"
```

Ou pour voir les logs d'erreur uniquement :

```logql
{job="crud-api", application="crud-api"} | json | level="ERROR"
```

**Vous devriez voir vos logs apparaître !**

### Requêtes utiles

- Tous les logs : `{job="crud-api"}`
- Logs d'erreur : `{job="crud-api"} |= "ERROR"`
- Logs 404 : `{job="crud-api"} |= "404"`
- Logs par environnement : `{job="crud-api", environment="production"}`

## Étape 4: Configurer le Webhook Discord

1. Sur Discord, allez dans **Paramètres du serveur** → **Intégrations** → **Webhooks**
2. Cliquez sur **Nouveau Webhook**
3. Donnez un nom (ex: "Grafana Alerts")
4. Sélectionnez le canal où envoyer les alertes
5. Copiez l'URL du webhook (format: `https://discord.com/api/webhooks/...`)

## Étape 5: Créer l'alerte 404 dans Grafana

### 5.1 Créer un Contact Point Discord

1. Dans Grafana, allez dans **Alerting** → **Contact points**
2. Cliquez sur **New contact point**
3. Configurez :
   - **Name** : `Discord - CRUD API`
   - **Integration** : `Discord`
   - **Webhook URL** : Collez votre URL Discord webhook
   - **Message** : (optionnel)
   ```
   ⚠️ **Alerte CRUD API**
   {{ .Annotations.summary }}

   **Détails:** {{ .Annotations.description }}
   ```
4. Cliquez sur **Test** pour vérifier
5. Cliquez sur **Save contact point**

### 5.2 Créer la règle d'alerte pour les 404

1. Allez dans **Alerting** → **Alert rules**
2. Cliquez sur **New alert rule**
3. Configurez :

**Section 1 - Set a query and alert condition**

- **Name** : `CRUD API - Plus de 5 erreurs 404 en 1 minute`
- **Query A** - Requête LogQL :
```logql
sum(count_over_time({job="crud-api"} |= "404" [1m]))
```

- **Expression B** - Threshold :
  - Operation: `Last`
  - Input: `A`

- **Expression C** - Condition :
  - Operation: `Threshold`
  - Input: `B`
  - IS ABOVE: `5`

- **Set as alert condition** : Sélectionnez `C`

**Section 2 - Set evaluation behavior**

- **Folder** : Créez un nouveau dossier "CRUD API Alerts" ou utilisez un existant
- **Evaluation group** : Créez "CRUD API Monitoring"
- **Evaluation interval** : `1m` (évalue toutes les minutes)
- **Pending period** : `0s` (alerte immédiatement)

**Section 3 - Add annotations**

- **Summary** :
```
Plus de 5 erreurs 404 détectées sur CRUD API
```

- **Description** :
```
{{ $values.C.Value }} erreurs 404 ont été détectées dans la dernière minute sur l'application CRUD API.

Vérifiez les logs pour identifier la cause.
```

**Section 4 - Notifications**

- **Contact point** : Sélectionnez `Discord - CRUD API`

4. Cliquez sur **Save rule and exit**

## Étape 6: Tester l'alerte

Générez plus de 5 erreurs 404 en 1 minute :

```bash
for i in {1..6}; do
  curl http://localhost:8080/page-inexistante
  sleep 5
done
```

Vous devriez recevoir une notification Discord dans environ 1 minute !

## Étape 7: Créer d'autres alertes utiles

### Alerte pour erreurs 500

Requête LogQL :
```logql
sum(count_over_time({job="crud-api"} |= "500" [5m])) > 3
```

### Alerte pour erreurs dans error.log

Requête LogQL :
```logql
sum(count_over_time({job="crud-api"} | json | level="ERROR" [5m])) > 10
```

### Alerte si l'application ne log plus (downtime)

Requête LogQL :
```logql
sum(count_over_time({job="crud-api"} [2m])) < 1
```

## Dépannage

### Fluent Bit ne se connecte pas à Loki

Vérifiez les logs :
```bash
docker logs crud-fluent-bit
```

Erreurs courantes :
- **connection refused** : Vérifiez LOKI_HOST et LOKI_PORT
- **401 Unauthorized** : Vérifiez LOKI_USER et LOKI_PASSWORD
- **certificate verify failed** : Essayez `LOKI_TLS_VERIFY=Off` (non recommandé en production)

### Les logs n'apparaissent pas dans Grafana

1. Vérifiez que Fluent Bit tourne : `docker ps | grep fluent-bit`
2. Vérifiez les logs Fluent Bit : `docker logs crud-fluent-bit`
3. Dans Grafana Explorer, vérifiez les labels disponibles :
```logql
{job=~".+"}
```

4. Vérifiez la plage de temps dans Grafana (en haut à droite, essayez "Last 5 minutes")

### L'alerte ne se déclenche pas

1. Testez la requête dans **Explore** d'abord
2. Vérifiez que la requête retourne une valeur > 5
3. Attendez 1-2 minutes (délai d'évaluation)
4. Vérifiez l'état de l'alerte dans **Alerting** → **Alert rules**

### Discord ne reçoit pas les notifications

1. Testez le contact point : **Alerting** → **Contact points** → **Test**
2. Vérifiez que le webhook Discord est actif
3. Vérifiez les permissions du bot Discord

## Commandes utiles

```bash
# Redémarrer Fluent Bit
docker-compose restart fluent-bit

# Voir les logs Fluent Bit en temps réel
docker logs crud-fluent-bit -f

# Vérifier que les fichiers sont montés correctement
docker exec crud-fluent-bit ls -la /fluent-bit/etc/
docker exec crud-fluent-bit ls -la /var/logs/crud/

# Tester la connectivité Loki depuis Fluent Bit
docker exec crud-fluent-bit ping your-loki-host.run.app
```

## Architecture

```
┌─────────────────┐
│   CRUD API      │
│  (Spring Boot)  │
└────────┬────────┘
         │ Écrit logs
         ▼
┌─────────────────┐
│  logs/*.log     │
│  - app.log      │
│  - error.log    │
│  - access.log   │
└────────┬────────┘
         │ Lit (tail)
         ▼
┌─────────────────┐
│  Fluent Bit     │
│  (Container)    │
└────────┬────────┘
         │ Envoie (HTTPS)
         ▼
┌─────────────────┐
│  Loki (GCP)     │
└────────┬────────┘
         │ Query
         ▼
┌─────────────────┐
│    Grafana      │
│  - Explorer     │
│  - Alerting     │
└────────┬────────┘
         │ Webhook
         ▼
┌─────────────────┐
│    Discord      │
└─────────────────┘
```

## Ressources

- [Documentation Fluent Bit](https://docs.fluentbit.io/)
- [Documentation Loki](https://grafana.com/docs/loki/latest/)
- [LogQL Query Language](https://grafana.com/docs/loki/latest/logql/)
- [Grafana Alerting](https://grafana.com/docs/grafana/latest/alerting/)
