# Lessons Learned

- Regel: Bei erneutem Push immer zuerst prüfen, ob Zielklasse (hier `AdminStatsCommand`) strukturell geändert wurde, bevor alte Patches blind übernommen werden.
- Regel: Für Auth-Token-Fallbacks im CLI eine eindeutige Reihenfolge definieren (Option > Env > System-Property > Default) und zentral im Helper kapseln.
- Regel: Bei HTTP 401 in CLI-Kommandos immer eine direkte, ausführbare Hint-Meldung ausgeben.
- Regel: Bei Statistik-Bugs zuerst den betroffenen Metrikbereich isolieren (hier LOC) und andere Kennzahlen unverändert lassen, um Seiteneffekte zu vermeiden.
- Regel: Für bekannte Ausreißer (z. B. Repomix-Commits) eine explizite, zentral gepflegte Exklusionsliste einführen statt ad-hoc Filter an mehreren Stellen zu verteilen.
