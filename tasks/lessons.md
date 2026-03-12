# Lessons Learned

- Bei neuen Anforderungen zuerst explizit im Planungsmodus arbeiten und einen verifizierbaren Plan in `tasks/todo.md` festhalten.
- Bei Architekturfragen immer zunächst den Ist-Zustand im Repository prüfen, bevor eine Umsetzungsskizze erstellt wird.
- Akzeptanzkriterien pro Issue in konkrete Verifizierungsschritte übersetzen (Unit, Integration, E2E, fachliche Prüfpunkte).
- Wenn der Nutzer explizit "implementiere" verlangt, keine reine Doku liefern, sondern lauffähige End-to-End-Umsetzung inkl. Nutzungsanleitung.
- Für Uni-Projekte bevorzugt eine einfache, robuste Lösung (minimaler Scope, klare Commands), statt überkomplexer Architektur.
- Bei Spring-Services mit mehr als einem Konstruktor immer einen eindeutigen Injection-Konstruktor (`@Autowired`) definieren, um Umfeld-spezifische Instanziierungsfehler zu vermeiden.
