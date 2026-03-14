# TODO – US-C4 Segmentiertes Laden

## Plan
- [x] Ist-Analyse der Download- und Adapter-Struktur durchführen (CLI/Router/Edge).
- [x] Segment-Download im CLI als einfachen, testbaren Befehl ergänzen (parallel + Retry).
- [x] Edge-Download-Endpoint für Byte-Range unterstützen, damit echte Segment-Requests möglich sind.
- [x] Fehlerhafte/ungültige Segmente erkennen und Retry implementieren.
- [x] Minimale Tests und Build-Checks ausführen.
- [x] Testanleitung dokumentieren.

## Review
- Segmentierter Download wurde als neuer CLI-Befehl `user file download-segmented` ergänzt.
- Segmente können per Router dynamisch auf Edge-URLs verteilt oder über `--edge` explizit auf mehrere Edges gelegt werden.
- Ungültige Segmente (Status/Länge) führen zu Retry je Segment.
- Edge unterstützt nun Byte-Range-Requests (`Range`) mit `206 Partial Content` + `Content-Range`.
- Build/Test konnte wegen externer Maven-Repository-Restriction (HTTP 403 auf Maven Central) nicht vollständig ausgeführt werden.
