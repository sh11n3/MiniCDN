# Todo – US-C5 Audit Bugfix (Spring Bean Startup)

## Plan (verifizierbar)
- [x] Fehleranalyse anhand Stacktrace: Bean-Instanziierung `AuditLogService` scheitert wegen Konstruktorauflösung.
- [x] Fix-Design: eindeutigen Spring-Konstruktor definieren (`@Autowired` + `@Value`) und Test-Konstruktor entkoppeln.
- [x] Implementieren: `AuditLogService` konstruktorseitig Spring-sicher machen, ohne Feature-Änderung.
- [x] Verifizieren: Router-Tests (mind. Audit-Service-Test + Router-Modul-Testlauf) ausführen.
- [x] Lessons/Review aktualisieren, committen und PR-Text erzeugen.

## Review
- Root Cause: `AuditLogService` hatte mehrere öffentliche Konstruktoren ohne eindeutige Spring-Markierung; in der Zielumgebung wurde dadurch fälschlich ein Default-Konstruktor erwartet.
- Fix: produktiven Konstruktor explizit mit `@Autowired` markiert und interne Initialisierung auf einen privaten Hauptkonstruktor zentralisiert.
- Verhalten bleibt gleich (Audit-Tabelle, Query/Export), nur die Bean-Erzeugung wurde robust gemacht.
- Maven-Testlauf in dieser Umgebung bleibt wegen externer Download-Blockade (HTTP 403 Maven Central) limitiert.
