# NYC Transit Tracker

Queries the [MTA Bus Time](https://bustime.mta.info/) SIRI **stop-monitoring** API for a bus stop ID and shows upcoming arrivals (route, destination, vehicle location, expected arrival, stops away).

- **CLI:** `App` reads a stop ID and prints to the terminal.
- **Web API (Spring Boot):** `GET /api/stops/{stopId}/arrivals` returns the same data as JSON.

## Requirements

- **Java 14+** (see `java.version` in `pom.xml`)
- **Maven 3.x**
- An **MTA Bus Time API key** (obtain through MTA’s Bus Time / developer signup process)
- **PostgreSQL** reachable at the URL you configure (defaults in `application.properties` assume `localhost:5432` and database `nyc_transit`) — only needed for **`mvn spring-boot:run`**, not for `mvn test`

## Configuration

Do **not** commit a real API key. This repo ignores `src/main/resources/config.properties` (see `.gitignore`).

**Option A — environment variable (good for CI and public clones)**

Set `MTA_API_KEY` to your key. The application reads this first.

Examples:

- **Windows (PowerShell, current session):**  
  `$env:MTA_API_KEY = "your-key-here"`
- **Git Bash:**  
  `export MTA_API_KEY='your-key-here'`

**Option B — local properties file**

1. Copy `src/main/resources/config.properties.example` to `src/main/resources/config.properties`.
2. Replace `YOUR_MTA_API_KEY_HERE` with your key.

### PostgreSQL (Spring Boot / JPA)

The web app uses **PostgreSQL** when you run `mvn spring-boot:run`. Connection settings are read from **`SPRING_DATASOURCE_*`** environment variables. If you omit them, [`application.properties`](src/main/resources/application.properties) uses local defaults (`localhost:5432`, database `nyc_transit`, user/password `nyc`).

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/nyc_transit` |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |

**Windows (PowerShell, current session):**

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/nyc_transit"
$env:SPRING_DATASOURCE_USERNAME = "nyc"
$env:SPRING_DATASOURCE_PASSWORD = "nyc"
```

**Git Bash / macOS / Linux:**

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/nyc_transit'
export SPRING_DATASOURCE_USERNAME='nyc'
export SPRING_DATASOURCE_PASSWORD='nyc'
```

Start PostgreSQL before the app (or use Docker / cloud Postgres). **`mvn test`** does not require Postgres: tests use an in-memory H2 database via `src/test/resources/application.properties`.

## Build and run

From the project root:

```bash
mvn compile exec:java
```

When prompted, enter a **stop ID** (numeric string used by MTA Bus Time). Example: `404271`.

**Non-interactive:** pass the stop ID as the first program argument (useful for scripts and demos):

```bash
mvn compile exec:java -Dexec.args="404271"
```

On **PowerShell**, if `-Dexec.args` is split incorrectly, quote it: `"-Dexec.args=404271"`.

Run tests only:

```bash
mvn test
```

### REST API (Spring Boot)

Start the embedded server (default port **8080**):

```bash
mvn spring-boot:run
```

### Two-terminal workflow (Spring Boot API + React UI)

This repo can be run with **two terminals** during development:

- **Terminal A (backend: API on `http://localhost:8080`)**
  1. Start Postgres (if using Docker Compose):

     ```bash
     docker compose up -d
     ```

  2. Set your MTA key (or use `src/main/resources/config.properties`):

     - **Windows (PowerShell):** `$env:MTA_API_KEY = "your-key-here"`
     - **Git Bash:** `export MTA_API_KEY='your-key-here'`

  3. Start Spring Boot:

     ```bash
     mvn spring-boot:run
     ```

- **Terminal B (frontend: React dev server on `http://localhost:5173`)**

  ```bash
  cd frontend
  npm install
  npm run dev
  ```

Then open `http://localhost:5173/` in your browser.

With a real MTA key configured (`MTA_API_KEY` or `config.properties`), fetch arrivals for a stop:

```bash
curl "http://localhost:8080/api/stops/404271/arrivals"
```

- **200** with a JSON array: success. An **empty array `[]`** means no upcoming buses for that stop (not an error).
- **502** if the MTA could not be reached or returned no usable body (`{"error":"..."}`).
- **500** if the response could not be parsed (`{"error":"..."}`).

Stop the server with **Ctrl+C** in that terminal.

### Favorite stops (`/api/favorite-stops`)

Persisted in PostgreSQL (same datasource as above). After listing or adding favorites, clients still call **`GET /api/stops/{stopId}/arrivals`** for live arrival data.

| Method | Path | Body | Success |
|--------|------|------|---------|
| **GET** | `/api/favorite-stops` | — | **200** — JSON array of `{ "stopId", "stopName" }` (may be `[]`) |
| **POST** | `/api/favorite-stops` | JSON object, e.g. `{ "stopId": "404271", "stopName": "My stop" }` (`stopName` optional) | **200** — saved favorite as JSON; **400** if `stopId` is missing or blank (`{"error":"..."}`) |
| **DELETE** | `/api/favorite-stops/{stopId}` | — | **204** — no body (including when the id was not stored; idempotent delete) |

Examples:

```bash
curl "http://localhost:8080/api/favorite-stops"
curl -X POST "http://localhost:8080/api/favorite-stops" \
  -H "Content-Type: application/json" \
  -d "{\"stopId\":\"404271\",\"stopName\":\"Example\"}"
curl -X DELETE "http://localhost:8080/api/favorite-stops/404271"
```

### Local PostgreSQL with Docker Compose

If you use the included [`docker-compose.yml`](docker-compose.yml), Postgres listens on **localhost:5432**. Credentials are provided via environment variables (or a local `.env` file). You can start from [`.env.example`](.env.example) (copy to `.env`, which is gitignored). Start Postgres with `docker compose up -d` (or `docker-compose up -d`) before `mvn spring-boot:run`.

## Project layout

| Piece | Role |
|--------|------|
| `NycTransitApplication` | Spring Boot entry point |
| `com.example.controller.BusController` | REST: `/api/stops/{stopId}/arrivals` |
| `com.example.controller.FavoriteStopController` | REST: `/api/favorite-stops` (list, add, delete) |
| `com.example.entity.FavoriteStop` | JPA entity for a saved stop |
| `com.example.repository.FavoriteStopRepository` | Spring Data JPA for favorites |
| `App` | CLI: reads stop ID, prints results |
| `BusService` | Orchestrates fetch + parse; returns `ArrivalsResult` |
| `ArrivalsResult` | Success vs fetch/parse failure for API and CLI |
| `MTAFetcher` | HTTP GET to the MTA JSON endpoint |
| `BusInfoParser` | Jackson: JSON → `BusInfo` list |
| `BusInfo` | One upcoming arrival at the stop |

## Limitations

- Uses a fixed path into the SIRI JSON (`StopMonitoringDelivery[0]`, etc.); unusual API responses may need parser tweaks.
- The primary interface is still the JSON API. A learning UI exists at `src/main/resources/static/ui-detour/`, and a React dev UI scaffold lives in `frontend/` (work in progress).

