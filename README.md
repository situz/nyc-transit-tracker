# NYC Transit Tracker

Queries the [MTA Bus Time](https://bustime.mta.info/) SIRI **stop-monitoring** API for a bus stop ID and shows upcoming arrivals (route, destination, vehicle location, expected arrival, stops away).

- **CLI:** `App` reads a stop ID and prints to the terminal.
- **Web API (Spring Boot):** JSON endpoints for arrivals and saved favorite stops.
- **Web UI (React):** Browse favorites and load live arrivals from the API.

## Quick start

**Prerequisites:** [Docker Desktop](https://www.docker.com/products/docker-desktop/) (or Docker Engine + Compose), **Node.js LTS + npm**, and an [MTA Bus Time API key](https://bustime.mta.info/).

1. **Clone the repo** and `cd` into the project root.

2. **Configure secrets** — copy [`.env.example`](.env.example) to **`.env`** (gitignored) and set your real key:

   ```env
   MTA_API_KEY=your-mta-key-here
   ```

   Postgres defaults in `.env` (`nyc` / `nyc_transit`) match [`docker-compose.yml`](docker-compose.yml).

3. **Start API + database** (repo root):

   ```bash
   docker compose up --build
   ```

   Add **`-d`** to run in the background. This starts **Postgres** (port **5432**) and the **Spring Boot API** (port **8080**). First build may take a few minutes.

4. **Start the React UI** (new terminal):

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. **Open** [http://localhost:5173](http://localhost:5173). The UI calls the API at [http://localhost:8080](http://localhost:8080).

**Quick checks**

```bash
curl http://localhost:8080/api/favorite-stops
curl "http://localhost:8080/api/stops/404271/arrivals"
```

**Stop:** `Ctrl+C` in the Compose terminal (foreground), or `docker compose down`. Stop the Vite dev server with `Ctrl+C` in the frontend terminal.

**Ports**

| Service | URL |
|---------|-----|
| React (dev) | `http://localhost:5173` |
| API | `http://localhost:8080` |
| PostgreSQL (host) | `localhost:5432` |

Other run modes (Maven on your PC, manual `docker run`): see [Build and run](#build-and-run) below.

## Requirements

- **Java 14+** (see `java.version` in `pom.xml`)
- **Maven 3.x**
- An **MTA Bus Time API key** (obtain through MTA’s Bus Time / developer signup process)
- **PostgreSQL** — bundled via Docker Compose for the [Quick start](#quick-start); also needed for **`mvn spring-boot:run`**, not for **`mvn test`**
- **Node.js LTS + npm** — React app under [`frontend/`](frontend/)
- **Docker + Compose** — recommended for API + Postgres ([Quick start](#quick-start))

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

The [Quick start](#quick-start) above is the usual path. This section covers the **CLI**, **tests**, **CI**, and **alternative** ways to run the API.

### CLI

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

This repository includes a **CI workflow** ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)) that runs on every **push** and **pull request** to `main` or `master`. Two jobs run in parallel on a clean **Ubuntu** virtual machine:

| Job | What it runs |
|-----|----------------|
| **`test`** | `mvn -B test` (Java + Maven, with dependency caching) |
| **`build-frontend`** | `npm install` and `npm run build` in [`frontend/`](frontend/) |

If either job fails, the workflow fails and GitHub shows a red **X** on the commit or PR. **No MTA API key** or Postgres is required for CI (`mvn test` uses in-memory H2).

Locally, `mvn test` pins the test datasource via Surefire in [`pom.xml`](pom.xml) so **your** `SPRING_DATASOURCE_*` shell variables (used for real Postgres during dev) do not override in-memory H2 for tests.

### REST API (Spring Boot)

Start the embedded server (default port **8080**):

```bash
mvn spring-boot:run
```

### Two-terminal workflow (Spring Boot on your PC + React UI)

Run the API with **Maven** on your machine (not the Compose `api` service). Start **only Postgres** from Compose so port **8080** is free for Spring Boot:

- **Terminal A (backend: API on `http://localhost:8080`)**
  1. Start Postgres only (do **not** run the full stack here — `docker compose up` also starts the `api` service on 8080):

     ```bash
     docker compose up -d postgres
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

### Docker Compose (details)

Same steps as [Quick start](#quick-start). [`docker-compose.yml`](docker-compose.yml) runs **Postgres** and the **`api`** service; the API uses hostname **`postgres`** for JDBC inside the Compose network.

| Where the API runs | JDBC host for Postgres | Why |
|--------------------|-------------------------|-----|
| `mvn spring-boot:run` on your PC | `localhost` | App and DB both on your machine. |
| API container, Postgres via Compose on the **host** only | `host.docker.internal` | Container reaches your PC’s port 5432. |
| **Both API and Postgres in Compose** | **`postgres`** | Compose DNS: service name = hostname on the shared network. |

### Local PostgreSQL only (Compose) + API on your PC

Postgres alone: `docker compose up -d postgres`, then `mvn spring-boot:run` with `SPRING_DATASOURCE_*` pointing at `localhost:5432`.

### Run the API in Docker (manual `docker run`)

Alternative to the Compose **`api`** service above. The root [`Dockerfile`](Dockerfile) packages the **Spring Boot API** (not the React dev server). It uses a **multi-stage build**: stage 1 compiles with Maven; stage 2 runs only the JAR on a slim Java 17 runtime. [`.dockerignore`](.dockerignore) keeps `target/`, `frontend/`, and secrets out of the build context.

**1. Build the image** (from repo root):

```bash
docker build -t nyc-transit-api .
```

**2. Start Postgres only** (do not start the Compose `api` service — it also uses port 8080):

```bash
docker compose up -d postgres
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

## CORS

Browsers only call the API from origins listed in **`CORS_ALLOWED_ORIGINS`** (comma-separated). Default: `http://localhost:5173` (Vite dev server).

| Where you run the API | How to set it |
|----------------------|---------------|
| **Docker Compose** | Add to `.env`: `CORS_ALLOWED_ORIGINS=http://localhost:5173` then `docker compose up --build` |
| **EC2 `docker run`** | `-e CORS_ALLOWED_ORIGINS='http://localhost:5173,https://your-site.com'` |
| **Maven locally** | PowerShell: `$env:CORS_ALLOWED_ORIGINS='http://localhost:5173'; mvn spring-boot:run` |

After changing origins you only need to **restart** the API (no new image build unless you changed Java code). Config lives in [`CorsConfig.java`](src/main/java/com/example/config/CorsConfig.java) and [`application.properties`](src/main/resources/application.properties).

**Local React + cloud API:** keep `http://localhost:5173` in the list; the browser still sends that origin even when `fetch` targets your EC2 IP.

### Test local UI against cloud API (Path A)

Use this before deploying the frontend. The React dev server stays on your PC; only the **API URL** points at EC2.

**Prerequisites:** EC2 API is running (`curl http://YOUR_EC2_IP:8080/api/favorite-stops` returns `200`). EC2 container has `CORS_ALLOWED_ORIGINS=http://localhost:5173`.

1. **Create** `frontend/.env.local` (gitignored):

   ```env
   VITE_API_URL=http://107.21.84.223:8080
   ```

   Replace with your EC2 public IP. No trailing slash.

2. **Do not** run local API on port 8080 (optional — Path A only needs EC2). Stop `docker compose` if it would conflict; not required if you are not using local API.

3. **Start the UI** (new terminal):

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **Open** [http://localhost:5173](http://localhost:5173).

5. **Try it:**
   - Page loads without a red error → GET favorites worked.
   - Add a stop ID (e.g. `404271`) → POST worked.
   - Click the stop → arrivals load (needs a valid MTA stop ID).

6. **If something fails**, open browser DevTools (F12) → **Console** or **Network**:
   - **CORS error** → EC2 missing `http://localhost:5173` in `CORS_ALLOWED_ORIGINS`; restart API container.
   - **Failed to fetch** / timeout → EC2 stopped, security group blocks port 8080, or wrong IP in `.env.local`.
   - **502 on arrivals** → MTA API issue, not CORS.

7. **Switch back to local API:** delete `frontend/.env.local` or set `VITE_API_URL=http://localhost:8080`, then restart `npm run dev`.

`App.jsx` reads `VITE_API_URL`; see [`frontend/.env.example`](frontend/.env.example).

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
| `CorsConfig` | CORS for `/api/**`; origins from `CORS_ALLOWED_ORIGINS` |
| `frontend/` | React (Vite) UI — dev server (`npm run dev`), build output in `frontend/dist/` |
| `Dockerfile` | Multi-stage image for the Spring Boot API |
| `docker-compose.yml` | Compose: Postgres + API ([Quick start](#quick-start)) |
| `.dockerignore` | Files excluded from `docker build` context |

## Limitations

- Uses a fixed path into the SIRI JSON (`StopMonitoringDelivery[0]`, etc.); unusual API responses may need parser tweaks.
- The JSON API is the contract for integrations. A small learning page lives at `src/main/resources/static/ui-detour/`. The main browser UI is the React app in `frontend/` (run via `npm run dev`; production build via `npm run build`).

