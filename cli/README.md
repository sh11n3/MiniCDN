# Mini-CDN CLI

## Datei-Download (User Story US-M2)

**Befehlssyntax**

```
minicdn user file download <remotePath> --router <routerBaseUrl> --region <region> --out <localPath> [--overwrite]
```

**Beispiel**

```
minicdn user file download docs/handbuch.pdf \
  --router http://localhost:8081 \
  --region eu-central \
  --out ./downloads/handbuch.pdf
```

**Exit-Codes**

* `0` – Download erfolgreich
* `1` – Client-seitiger Fehler (Validierung/Netzwerk/Schreibfehler)
* `2` – Anfrage abgelehnt (HTTP 4xx außer 404)
* `3` – Datei nicht gefunden (HTTP 404)
* `4` – Server-/Netzwerkproblem (HTTP 5xx)
