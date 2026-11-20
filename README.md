# rsikspr-ev-planner

Planer putovanja električnim vozilom (RSIKSPR, 1. laboratorijska vježba).
Spring Boot 4, Java 25, PostgreSQL 17 (Docker), Flyway, JGraphT.

Aplikacija za zadano vozilo, polaznu i odredišnu punionicu i stanje baterije izračuna najbržu
rutu s punjenjima (Dijkstra na grafu punionica, formule F1–F8) i spremi je u bazu.

## Preduvjeti

- JDK 25 (Gradle toolchain ga koristi; `JAVA_HOME` mora pokazivati na JDK 25)
- Docker (Docker Desktop na Windowsima)

## Baza

Baza se pokreće iz korijena projekta (gdje je `compose.yaml`):

```bash
docker compose up -d      # pokreni (prvi put stvara bazu)
docker compose stop       # zaustavi (podaci ostaju)
docker compose down -v    # obriši bazu zajedno s podacima
```

PostgreSQL sluša na **`localhost:15432`** (ne 5432, jer na računalu već radi lokalni
PostgreSQL). Baza, korisnik i lozinka su `evplanner`.

Shemu stvara Flyway pri pokretanju aplikacije:

- `V1__graph.sql` – punionice (`station`, 654) i veze (`station_link`), zadano
- `V2__init.sql` – vozila (`vehicle`), putovanja (`trip`) i zaustavljanja (`trip_stop`)

## Aplikacija

```bash
./gradlew bootRun         # Windows: gradlew.bat bootRun
```

Aplikacija radi na <http://localhost:8080>.

- **Swagger UI:** <http://localhost:8080/swagger-ui.html>
- OpenAPI opis: <http://localhost:8080/v3/api-docs>
- Zdravlje: <http://localhost:8080/actuator/health> (`UP`, a `DOWN` kada je baza zaustavljena)

Primjeri svih poziva, uključujući pogreške, su u [`http/ev-planner.http`](http/ev-planner.http)
(IntelliJ HTTP klijent ili VS Code REST Client).

## API

| Operacija | Opis |
|---|---|
| `POST /api/v1/vehicles` | kreira vozilo, vraća 201 i `Location` |
| `GET /api/v1/vehicles/{id}` | dohvaća vozilo |
| `GET /api/v1/stations?page=&size=` | popis punionica s paginacijom (poredano po id, `size` ≤ 200) |
| `GET /api/v1/stations/{id}` | dohvaća punionicu |
| `POST /api/v1/trips` | izračuna i spremi putovanje, vraća 201 i `Location` |
| `GET /api/v1/trips/{id}` | dohvaća spremljeno putovanje |

Pogreške se vraćaju kao `application/problem+json` (ProblemDetail):
400 neispravan zahtjev (navedena su neispravna polja), 404 resurs ne postoji,
422 odredište nije dostižno.

## Testovi

```bash
./gradlew test            # Windows: gradlew.bat test
```

Testovi ne trebaju bazu:

- `FormulasTest` – formule F1–F8 na primjeru iz tablice 7.2
- `TripPlannerTest` – planer na `src/test/resources/test-graph.json`, svih 5 primjera iz tablice 7.3
- `VehicleControllerTest`, `StationControllerTest`, `TripControllerTest` – `@WebMvcTest`
  s `@MockitoBean`, uspješni i neuspješni scenariji (201, 400, 404, 422)

## Struktura

```
src/main/java/hr/fer/rsikspr/ev_planner/
├── error/     ProblemDetail obrada pogrešaka (GlobalExceptionHandler)
├── planner/   Formulas i TripPlanner – izračun rute, bez Springa i baze
├── station/   punionice i veze (samo čitanje)
├── trip/      putovanja i zaustavljanja
└── vehicle/   vozila
```
