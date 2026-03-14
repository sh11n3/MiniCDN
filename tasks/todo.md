# TODO – US-C4 Segmentiertes Laden (Nachschärfung CLI-Feedback + Help)

## Plan
- [x] Bestehende `download-segmented` Implementierung und CLI-Help auf aktualisiertem Projektstand prüfen.
- [x] CLI-Hilfetexte auf Default-Host-Nutzung ausrichten (ohne erzwungenes `-H`) und Beispiele für Segmentierung ergänzen.
- [x] Sichtbares, segmentbezogenes CLI-Feedback beim segmentierten Download ergänzen (Retries + erfolgreiche Segmente).
- [x] Validierung für `--segments`/`--retries` auf Command-Ebene klar erzwingen und Common-Utilities konsistent nutzen.
- [x] Relevante Unit-Tests für Segmentierungslogik ergänzen und lokal ausführen.

## Review
- Die Help-Footer für `user file` und `user file download` wurden auf den aktuellen Default-Host-Flow umgestellt (ohne Pflicht für `-H`) und um segmentierte Beispiele ergänzt.
- `download-segmented` zeigt jetzt im CLI sichtbar, dass segmentiert geladen wird: Modus-Info, Retry-Events pro Segment und erfolgreiche Segmentbereiche inkl. Edge-Quelle.
- Command-seitige Eingabevalidierung für `--segments` (>0) und `--retries` (>=0) wurde ergänzt.
- Im Service wurde ein optionaler `SegmentProgressListener` eingeführt, um Fortschritt sauber an die CLI zu melden, ohne Service/Adapter-Grenzen zu brechen.
- Die Remote-Pfad-Normalisierung nutzt konsistent `PathUtils.normalizeRelativePath` aus `common`.
- Ein zusätzlicher Unit-Test deckt Segment-Capping auf Dateigröße ab.
