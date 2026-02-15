# Admin-Statistiken (Mini-CDN)

Diese Dokumentation beschreibt den neuen Endpoint `GET /api/cdn/admin/stats` und den CLI-Befehl `admin stats show`.

## Erfasste Metriken

- **Router**
  - `totalRequests`: Gesamtanzahl Requests seit Start
  - `requestsPerMinute`: Exakte Anzahl Requests im gewählten Zeitfenster (Standard: 60 Sekunden)
  - `activeClients`: Anzahl eindeutiger Client-IDs im Zeitfenster
  - `routingErrors`: Anzahl Routing-Fehler
  - `requestsByRegion`: Kumulierte Requests pro Region
- **Cache (aggregiert über Edges)**
  - `hits`
  - `misses`
  - `hitRatio`
  - `filesLoaded`: Aktuell gecachte Dateien über alle Edge-Nodes
- **Nodes**
  - `total`
  - `byRegion`

## API-Beispiel

```bash
curl "http://localhost:8080/api/cdn/admin/stats?windowSec=60&aggregateEdge=true"
```

### Beispiel-Response

```json
{
  "timestamp": "2026-02-15T10:00:00Z",
  "windowSec": 60,
  "router": {
    "activeClients": 2,
    "routingErrors": 0,
    "totalRequests": 3,
    "requestsPerMinute": 3,
    "requestsByRegion": {
      "EU": 3
    }
  },
  "cache": {
    "hits": 2,
    "misses": 1,
    "hitRatio": 0.6666,
    "filesLoaded": 1
  },
  "nodes": {
    "total": 1,
    "byRegion": {
      "EU": 1
    }
  },
  "edgeAggregation": {
    "enabled": true,
    "errors": []
  }
}
```

## CLI-Beispiele (JLine-Menü)

Innerhalb von `MiniCdnCli`:

```text
mini cdn >> admin stats show --host http://localhost:8080
mini cdn >> admin stats show --host http://localhost:8080 --window-sec 60 --aggregate-edge true --json
```

## Hinweise zum lokalen Betrieb

- Client-Tracking verwendet **Client-ID** (`clientId` Query-Parameter oder `X-Client-Id` Header), keine IP-Adressen.
- `requestsPerMinute` wird exakt über Zeitstempel im gewählten Zeitfenster berechnet.
- Der bestehende Legacy-Endpoint `/api/cdn/routing/metrics` bleibt unverändert verfügbar.
