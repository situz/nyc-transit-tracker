# NYC Transit Tracker

Small command-line program that queries the [MTA Bus Time](https://bustime.mta.info/) SIRI **stop-monitoring** API for a given bus stop ID and prints upcoming arrivals (route, destination, vehicle location, expected arrival, stops away).

## Requirements

- **Java 14+** (see `maven.compiler` in `pom.xml`)
- **Maven 3.x**
- An **MTA Bus Time API key** (obtain through MTA’s Bus Time / developer signup process)

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

## Project layout

| Piece | Role |
|--------|------|
| `App` | Reads stop ID from stdin, prints results |
| `BusService` | Orchestrates fetch + parse |
| `MTAFetcher` | HTTP GET to the MTA JSON endpoint |
| `BusInfoParser` | Jackson: JSON → `BusInfo` list |
| `BusInfo` | One upcoming arrival at the stop |

## Limitations

- Uses a fixed path into the SIRI JSON (`StopMonitoringDelivery[0]`, etc.); unusual API responses may need parser tweaks.
- Console app only; no map or mobile UI.

