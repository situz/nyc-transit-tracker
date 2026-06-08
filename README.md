# NYC Transit Tracker

Queries the [MTA Bus Time](https://bustime.mta.info/) SIRI **stop-monitoring** API for a bus stop ID and shows upcoming arrivals (route, destination, vehicle location, expected arrival, stops away).

- **CLI:** `App` reads a stop ID and prints to the terminal.
- **Web API (Spring Boot):** `GET /api/stops/{stopId}/arrivals` returns the same data as JSON.

## Requirements

- **Java 14+** (see `java.version` in `pom.xml`)
- **Maven 3.x**
- An **MTA Bus Time API key** (obtain through MTA’s Bus Time / developer signup process)
- **PostgreSQL** reachable at the URL you configure (defaults in `application.properties` assume `localhost:5432` and database `nyc_transit`) — only needed for **`mvn spring-boot:run`**, not for `mvn test`
- **Node.js LTS + npm** — only if you work on the React app under [`frontend/`](frontend/)
- **Docker** — optional, for running the API in a container ([`Dockerfile`](Dockerfile))

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

### Continuous integration (GitHub Actions)

This repository includes a **CI workflow** that runs on every **push** and **pull request** to `main` or `master`. It checks out the code on a clean **Ubuntu** virtual machine, installs **Java** and **Maven** (with dependency caching), and runs:

```bash
mvn -B test
```

If tests fail, the workflow fails and GitHub shows a red **X** on the commit or PR. You do not need to install anything on GitHub’s side beyond pushing this file: [`.github/workflows/ci.yml`](.github/workflows/ci.yml). **No MTA API key** is required for `mvn test` (tests use H2, not PostgreSQL).

Locally, `mvn test` pins the test datasource via Surefire in [`pom.xml`](pom.xml) so **your** `SPRING_DATASOURCE_*` shell variables (used for real Postgres during dev) do not override in-memory H2 for tests.

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

The React dev server runs on a **different port** than Spring Boot. [`com.example.config.CorsConfig`](src/main/java/com/example/config/CorsConfig.java) allows browser requests from **`http://localhost:5173`** to **`/api/**`** during development.

**First-time setup:** run `npm install` once in `frontend/` (after cloning). You do **not** commit `frontend/node_modules/` (see [`.gitignore`](.gitignore)).

#### Frontend production build

From `frontend/`:

```bash
npm run build
```

This writes optimized static files to **`frontend/dist/`** (also gitignored). That folder is what you would deploy to static hosting, or copy into Spring Boot’s `src/main/resources/static/` if you want the UI served from the same server as the API.

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

### Start API + Postgres with Docker Compose (one command)

[`docker-compose.yml`](docker-compose.yml) runs **both** Postgres and the Spring Boot **API** (`api` service). Compose puts them on a private network so the API connects to the database at hostname **`postgres`** (the service name), not `localhost`.

1. Copy [`.env.example`](.env.example) to **`.env`** (gitignored) if you do not have one yet, and set **`MTA_API_KEY`** to your real key.
2. From the repo root:

   ```bash
   docker compose up --build
   ```

   Add **`-d`** to run in the background. API: `http://localhost:8080`, Postgres on host port **5432**.

3. React dev UI (separate terminal): `cd frontend && npm run dev` → `http://localhost:5173`.

Stop: **Ctrl+C** (foreground) or `docker compose down`.

| Where the API runs | JDBC host for Postgres | Why |
|--------------------|-------------------------|-----|
| `mvn spring-boot:run` on your PC | `localhost` | App and DB both on your machine. |
| API container, Postgres via Compose on the **host** only | `host.docker.internal` | Container reaches your PC’s port 5432. |
| **Both API and Postgres in Compose** | **`postgres`** | Compose DNS: service name = hostname on the shared network. |

### Local PostgreSQL only (Compose) + API on your PC

Postgres alone: `docker compose up -d postgres`, then `mvn spring-boot:run` with `SPRING_DATASOURCE_*` pointing at `localhost:5432`.

### Run the API in Docker (manual `docker run`)

The root [`Dockerfile`](Dockerfile) packages the **Spring Boot API** (not the React dev server). It uses a **multi-stage build**: stage 1 compiles with Maven; stage 2 runs only the JAR on a slim Java 17 runtime. [`.dockerignore`](.dockerignore) keeps `target/`, `frontend/`, and secrets out of the build context.

**1. Build the image** (from repo root):

```bash
docker build -t nyc-transit-api .
```

**2. Start Postgres** (if not already running):

```bash
docker compose up -d
```

**3. Run the API container** with environment variables (never bake secrets into the image):

**Windows PowerShell** — Postgres on the host via Docker Compose; `host.docker.internal` reaches your PC from inside the container:

```powershell
docker run --rm -p 8080:8080 `
  -e MTA_API_KEY="your-mta-key" `
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/nyc_transit" `
  -e SPRING_DATASOURCE_USERNAME="nyc" `
  -e SPRING_DATASOURCE_PASSWORD="nyc" `
  nyc-transit-api
```

**Git Bash / macOS / Linux:**

```bash
docker run --rm -p 8080:8080 \
  -e MTA_API_KEY='your-mta-key' \
  -e SPRING_DATASOURCE_URL='jdbc:postgresql://host.docker.internal:5432/nyc_transit' \
  -e SPRING_DATASOURCE_USERNAME='nyc' \
  -e SPRING_DATASOURCE_PASSWORD='nyc' \
  nyc-transit-api
```

Then test: `curl http://localhost:8080/api/favorite-stops`

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
| `frontend/` | React (Vite) UI — dev server (`npm run dev`), build output in `frontend/dist/` |
| `Dockerfile` | Multi-stage image for the Spring Boot API |
| `.dockerignore` | Files excluded from `docker build` context |

## Limitations

- Uses a fixed path into the SIRI JSON (`StopMonitoringDelivery[0]`, etc.); unusual API responses may need parser tweaks.
- The JSON API is the contract for integrations. A small learning page lives at `src/main/resources/static/ui-detour/`. The main browser UI is the React app in `frontend/` (run via `npm run dev`; production build via `npm run build`).

