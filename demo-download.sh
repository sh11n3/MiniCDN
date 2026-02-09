#!/usr/bin/env bash
set -euo pipefail

echo "== Mini-CDN Download Demo =="
echo "Voraussetzungen:"
echo "1) Services starten (Router, Edge, Origin)"
echo "2) Beispieldatei im Origin vorhanden"
echo ""
echo "Beispiel (anpassen falls Ports anders sind):"
echo "  minicdn user file download docs/demo.txt --router http://localhost:8081 --region eu --out ./downloads/demo.txt"
echo ""
echo "Hinweis: Mit --overwrite kann eine bestehende Datei ersetzt werden."
