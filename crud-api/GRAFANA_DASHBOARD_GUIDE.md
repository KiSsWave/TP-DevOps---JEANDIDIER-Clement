# Guide pour améliorer le visuel des logs dans Grafana

Ce guide vous explique comment créer de magnifiques dashboards Grafana pour visualiser vos logs.

## Configuration préalable

Vos logs sont maintenant enrichis avec ces métadonnées :
- `log_type` : type de log (app, error, access)
- `level` : niveau du log (INFO, WARN, ERROR, DEBUG)
- `logger` : nom du logger Java
- `thread` : nom du thread
- `time` : timestamp du log
- `message` : message du log
- `application` : crud-api
- `environment` : dev/prod

## 1. Dashboard d'overview (Vue d'ensemble)

### Panel 1: Compteur de logs par niveau (Time Series)

**Type de visualisation**: Time series (Graphique)

**Requête LogQL**:
```logql
sum by (level) (count_over_time({job="crud-api"} | json [1m]))
```

**Configuration**:
- Legend: `{{level}}`
- Couleurs personnalisées:
  - ERROR: Rouge (#E02F44)
  - WARN: Orange (#FF9830)
  - INFO: Bleu (#3274D9)
  - DEBUG: Gris (#9FA7B3)

---

### Panel 2: Total de logs par seconde (Stat)

**Type de visualisation**: Stat (Nombre unique)

**Requête LogQL**:
```logql
sum(rate({job="crud-api"}[1m]))
```

**Configuration**:
- Unit: `logs/s`
- Color scheme: Green-Yellow-Red (by value)
- Thresholds: 0 (green), 10 (yellow), 50 (red)

---

### Panel 3: Distribution des niveaux de logs (Pie Chart)

**Type de visualisation**: Pie chart (Camembert)

**Requête LogQL**:
```logql
sum by (level) (count_over_time({job="crud-api"} | json [$__range]))
```

**Configuration**:
- Legend: Values + Percentages
- Donut mode: ON

---

### Panel 4: Taux d'erreurs (Time Series)

**Type de visualisation**: Time series

**Requête LogQL**:
```logql
sum(rate({job="crud-api", log_type="error"} [5m]))
```

**Configuration**:
- Fill opacity: 20
- Line width: 2
- Color: Rouge
- Show points: Auto

---

## 2. Dashboard des erreurs

### Panel 1: Top 10 des erreurs (Bar Gauge)

**Type de visualisation**: Bar gauge (Jauge horizontale)

**Requête LogQL**:
```logql
topk(10, sum by (logger) (count_over_time({job="crud-api", log_type="error"} [$__range])))
```

**Configuration**:
- Orientation: Horizontal
- Display mode: Gradient
- Show values: Always

---

### Panel 2: Timeline des erreurs (Logs)

**Type de visualisation**: Logs (Table de logs)

**Requête LogQL**:
```logql
{job="crud-api", log_type="error"} | json
```

**Configuration**:
- Show time: ON
- Wrap lines: ON
- Prettify JSON: ON
- Enable context: ON

---

### Panel 3: Erreurs récentes avec stack traces (Table)

**Type de visualisation**: Table

**Requête LogQL**:
```logql
{job="crud-api", log_type="error"}
| json
| line_format "{{.time}} [{{.thread}}] {{.logger}} - {{.message}}"
```

**Configuration**:
- Columns: time, level, logger, message
- Cell display mode: JSON View (pour message)

---

## 3. Dashboard de performance

### Panel 1: Logs par type (Time Series)

**Type de visualisation**: Time series

**Requête LogQL**:
```logql
sum by (log_type) (rate({job="crud-api"} [1m]))
```

**Configuration**:
- Stack: Normal
- Fill opacity: 80
- Legend: `{{log_type}}`

---

### Panel 2: Requêtes HTTP par statut (Logs panel)

**Type de visualisation**: Logs

**Requête LogQL**:
```logql
{job="crud-api", log_type="access"}
| json
| line_format "{{.status}} {{.request}} - {{.request_time}}s"
```

---

### Panel 3: Temps de réponse moyen (Stat)

**Type de visualisation**: Stat

**Requête LogQL** (si vous avez des métriques de temps):
```logql
avg_over_time({job="crud-api", log_type="access"}
| json
| unwrap request_time [5m])
```

---

## 4. Requêtes LogQL avancées

### Rechercher un texte spécifique

```logql
{job="crud-api"} |= "UserController"
```

### Exclure certains logs

```logql
{job="crud-api"} != "health" != "actuator"
```

### Filtrer par niveau ERROR ou WARN

```logql
{job="crud-api"} | json | level=~"ERROR|WARN"
```

### Extraire et afficher seulement le message

```logql
{job="crud-api"}
| json
| line_format "{{.message}}"
```

### Compter les erreurs 404

```logql
sum(count_over_time({job="crud-api"} |= "404" [5m]))
```

### Top 5 des endpoints les plus appelés

```logql
topk(5, sum by (request) (count_over_time({job="crud-api", log_type="access"} | json [$__range])))
```

### Logs avec stack traces Java

```logql
{job="crud-api"} |~ "\\tat .+\\(.+\\.java:\\d+\\)"
```

### Taux d'erreur en pourcentage

```logql
(
  sum(rate({job="crud-api", log_type="error"} [5m]))
  /
  sum(rate({job="crud-api"} [5m]))
) * 100
```

---

## 5. Variables de dashboard (pour rendre dynamique)

Ajoutez ces variables dans Dashboard Settings → Variables :

### Variable: Environment

- **Type**: Query
- **Query**: `label_values(environment)`
- **Multi-value**: ON

### Variable: Log Level

- **Type**: Custom
- **Values**: `INFO,WARN,ERROR,DEBUG`
- **Multi-value**: ON

### Variable: Time Range

- **Type**: Interval
- **Values**: `1m,5m,10m,30m,1h`

Utilisez-les dans vos requêtes :
```logql
{job="crud-api", environment="$environment"} | json | level=~"$level"
```

---

## 6. Alertes visuelles avec Transformations

### Panel: Erreurs critiques (avec seuils)

**Requête LogQL**:
```logql
sum(count_over_time({job="crud-api", log_type="error"} [5m]))
```

**Transformations** (dans l'onglet Transform):
1. Add field from calculation
   - Mode: Reduce row
   - Function: Last
2. Configure thresholds
   - 0-10: Vert
   - 10-50: Jaune
   - 50+: Rouge

---

## 7. Configuration de couleurs personnalisées

Dans **Panel Options** → **Standard options** → **Color scheme**:

### Pour les niveaux de logs:

```json
{
  "overrides": [
    {
      "matcher": { "id": "byName", "options": "ERROR" },
      "properties": [{ "id": "color", "value": { "mode": "fixed", "fixedColor": "red" } }]
    },
    {
      "matcher": { "id": "byName", "options": "WARN" },
      "properties": [{ "id": "color", "value": { "mode": "fixed", "fixedColor": "orange" } }]
    },
    {
      "matcher": { "id": "byName", "options": "INFO" },
      "properties": [{ "id": "color", "value": { "mode": "fixed", "fixedColor": "blue" } }]
    }
  ]
}
```

---

## 8. Layout recommandé pour un dashboard

```
┌─────────────────────────────────────────────────────────┐
│  Row 1: Overview                                        │
├──────────────┬──────────────┬──────────────┬───────────┤
│ Total Logs/s │ Total Errors │ Error Rate % │ Uptime    │
│   (Stat)     │   (Stat)     │   (Gauge)    │  (Stat)   │
├──────────────┴──────────────┴──────────────┴───────────┤
│                                                         │
│  Row 2: Logs Timeline                                  │
│  ┌───────────────────────────────────────────────────┐ │
│  │  Logs par niveau (Time Series - Stacked)          │ │
│  └───────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│  Row 3: Distribution                                    │
├────────────────────────────────┬────────────────────────┤
│  Distribution des niveaux      │  Top 10 Loggers        │
│  (Pie Chart)                   │  (Bar Gauge)           │
├────────────────────────────────┴────────────────────────┤
│  Row 4: Recent Logs                                     │
│  ┌───────────────────────────────────────────────────┐ │
│  │  Derniers logs (Logs Panel - filtrable)           │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 9. Templates de panels prêts à l'emploi

### JSON Template: Panel "Logs par niveau"

```json
{
  "datasource": "Loki",
  "targets": [
    {
      "expr": "sum by (level) (count_over_time({job=\"crud-api\"} | json [1m]))",
      "refId": "A"
    }
  ],
  "title": "Logs par niveau",
  "type": "timeseries",
  "fieldConfig": {
    "defaults": {
      "color": {
        "mode": "palette-classic"
      },
      "custom": {
        "fillOpacity": 80,
        "lineWidth": 2
      }
    },
    "overrides": [
      {
        "matcher": { "id": "byName", "options": "ERROR" },
        "properties": [
          { "id": "color", "value": { "fixedColor": "red", "mode": "fixed" } }
        ]
      },
      {
        "matcher": { "id": "byName", "options": "WARN" },
        "properties": [
          { "id": "color", "value": { "fixedColor": "orange", "mode": "fixed" } }
        ]
      },
      {
        "matcher": { "id": "byName", "options": "INFO" },
        "properties": [
          { "id": "color", "value": { "fixedColor": "blue", "mode": "fixed" } }
        ]
      }
    ]
  }
}
```

---

## 10. Conseils pour de belles visualisations

### Couleurs
- Utilisez une palette cohérente
- Rouge pour les erreurs
- Orange/Jaune pour les warnings
- Bleu/Vert pour les infos
- Gris pour les debug

### Typographie
- Titres clairs et concis
- Unités explicites (logs/s, %, ms)
- Descriptions dans les panels

### Organisation
- Groupez par thématique (Overview, Errors, Performance)
- Utilisez des Rows pour séparer
- Mettez les métriques importantes en haut

### Performance
- Limitez les time ranges pour les requêtes lourdes
- Utilisez des variables pour la réutilisabilité
- Préférez `rate()` à `count_over_time()` pour les time series

### Interactivité
- Activez les drill-downs (clic sur une barre → filtre)
- Utilisez des variables pour filtrer
- Ajoutez des liens vers d'autres dashboards

---

## 11. Exemples de dashboards par cas d'usage

### Dashboard "Production Monitoring"
- Taux d'erreurs en temps réel
- Top erreurs
- Santé de l'application
- Alertes critiques

### Dashboard "Debugging"
- Logs bruts filtrables
- Stack traces
- Recherche full-text
- Timeline détaillée

### Dashboard "Business Metrics"
- Requêtes par endpoint
- Utilisateurs actifs
- Temps de réponse
- Taux de succès

---

## 12. Export et partage

### Exporter un dashboard
1. Dashboard Settings → JSON Model
2. Copiez le JSON
3. Partagez avec votre équipe

### Importer un dashboard
1. Create → Import
2. Collez le JSON
3. Sélectionnez votre datasource Loki

---

## Ressources supplémentaires

- [Grafana Docs - Best Practices](https://grafana.com/docs/grafana/latest/best-practices/)
- [LogQL Cheat Sheet](https://grafana.com/docs/loki/latest/logql/)
- [Grafana Dashboards Gallery](https://grafana.com/grafana/dashboards/)
