# TODO – Neuimplementierung auf neu gepushtem Stand (AdminStatsCommand angepasst)

- [x] Neu gepushten Stand analysieren, insbesondere geänderten `AdminStatsCommand`.
- [x] Admin-Token-Auflösung robuster machen (Option > Env > System-Property > Default).
- [x] UX bei 401 verbessern: konkrete Hint-Ausgabe für Token-Setzung ergänzen.
- [x] Keine Testimplementierung/-anpassung durchführen (Anweisung beachtet).
- [x] Verifikation dokumentieren.

## Review
- `HttpUtils.newAdminRequestBuilder(URI, String)` nutzt jetzt eine klare Fallback-Reihenfolge: expliziter Token -> `MINICDN_ADMIN_TOKEN` -> `-Dminicdn.admin.token` -> `secret-token`.
- `AdminStatsCommand` wurde auf dem aktuellen Stand angepasst: Optionstext für `--admin-token` präzisiert und bei HTTP 401 wird eine direkte Hint-Meldung zur Token-Setzung ausgegeben.
- Keine Tests wurden implementiert oder geändert, wie gefordert.

---

# TODO – LOC-Bereinigung für Repomix-Commits (sosa/xuzh)

- [x] Anforderung analysiert: Nur LOC ausschließen, Commit-Zähler unverändert lassen.
- [x] Python-Skript angelegt/angepasst und LOC-Exklusionslogik für `sosa`/`xuzh` ergänzt.
- [x] Verifikation per Python-Syntaxcheck durchgeführt.

## Review
- Neue Konstante `EXCLUDED_LOC_IDENTITIES` ergänzt, um betroffene Identitäten zentral zu pflegen.
- LOC-Aggregation in Main- und All-Branches-Auswertung bedingt auf `exclude_loc` gemacht, sodass Additions/Deletions/Net für diese Nutzer auf `0` laufen.
- Commit-Anzahlen, MR-Counts, Timelog- und Issue-Referenz-Logik bleiben unverändert, damit nur der gewünschte Metrikbereich beeinflusst wird.
