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

With a real MTA key configured (`MTA_API_KEY` or `config.properties`), fetch arrivals for a stop:

```bash
curl "http://localhost:8080/api/stops/404271/arrivals"
```

- **200** with a JSON array: success. An **empty array `[]`** means no upcoming buses for that stop (not an error).
- **502** if the MTA could not be reached or returned no usable body (`{"error":"..."}`).
- **500** if the response could not be parsed (`{"error":"..."}`).

Stop the server with **Ctrl+C** in that terminal.

## Project layout

| Piece | Role |
|--------|------|
| `NycTransitApplication` | Spring Boot entry point |
| `api.BusController` | REST: `/api/stops/{stopId}/arrivals` |
| `App` | CLI: reads stop ID, prints results |
| `BusService` | Orchestrates fetch + parse; returns `ArrivalsResult` |
| `ArrivalsResult` | Success vs fetch/parse failure for API and CLI |
| `MTAFetcher` | HTTP GET to the MTA JSON endpoint |
| `BusInfoParser` | Jackson: JSON → `BusInfo` list |
| `BusInfo` | One upcoming arrival at the stop |

## Limitations

- Uses a fixed path into the SIRI JSON (`StopMonitoringDelivery[0]`, etc.); unusual API responses may need parser tweaks.
- No web UI yet (React, etc.); API returns JSON only.

