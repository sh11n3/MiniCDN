# US-C5 Audit – Nutzung (Abfrage & Export)

Diese Anleitung zeigt die minimale Nutzung der neuen Audit-Funktion im Router.

## 1) Router starten

Beispiel (aus Projektroot):

```bash
mvn -pl router spring-boot:run
```

## 2) Beispielaktionen erzeugen (mit User-Kontext)

Audit-Events werden automatisch für API-Requests geloggt, wenn `X-User-Id` gesetzt ist.

```bash
curl -i -H "X-User-Id: 1" -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/users"

curl -i -X DELETE -H "X-User-Id: 1" -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/users/99999"
```

Optional: Erfolgreicher Login erzeugt ebenfalls einen Audit-Eintrag für den gefundenen User.

```bash
curl -i -X POST -H "Content-Type: application/json" \
  -d '{"name":"alice"}' \
  "http://localhost:8082/api/cdn/auth/login"
```

## 3) Audit-Logs abfragen (JSON)

Pflichtparameter: `userId`

```bash
curl -s -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/audit?userId=1" | jq
```

Mit Filtern:

```bash
curl -s -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/audit?userId=1&result=FAILURE&action=DELETE%20/api/cdn/admin/users/99999" | jq
```

Zeitfilter (ISO-8601):

```bash
curl -s -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/audit?userId=1&from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z" | jq
```

## 4) Audit-Logs exportieren (CSV)

```bash
curl -s -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/audit/export?userId=1" \
  -o audit-user-1.csv
```

Mit Filter auf Fehlschläge:

```bash
curl -s -H "Authorization: Bearer $MINICDN_ADMIN_TOKEN" \
  "http://localhost:8082/api/cdn/admin/audit/export?userId=1&result=FAILURE" \
  -o audit-user-1-failures.csv
```

## Speicherort der Audit-Logs

Die Audit-Logs liegen in der SQLite-Datenbank des Routers in der Tabelle `audit_logs`.

- DB-Datei: `data/users.db`
- Tabelle: `audit_logs`
