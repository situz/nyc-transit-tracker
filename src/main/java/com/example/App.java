package com.example;

import java.util.List;
import java.util.Scanner;

// CLI entry: resolve stop ID, call BusService to fetch and parse the MTA response, then print each bus.
// Stop ID: first program argument (e.g. mvn exec:java -Dexec.args="404271") or typed at the prompt if no args.
public class App {
    public static void main(String[] args) {
        // CLI does not use the Spring context; wire the same services manually.
        BusService service = new BusService(new MTAFetcher());

        // 1) Decide where the stop ID comes from: command line (for scripts/IDE) or interactive prompt.
        final String stopId;
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            stopId = args[0].trim();
        } else {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Enter Stop ID: ");
                stopId = scanner.nextLine().trim();
            }
        }

        if (stopId.isEmpty()) {
            System.err.println("Stop ID is empty.");
            System.exit(1);
            return;
        }

        // 2) One call: distinguish errors from an empty schedule.
        ArrivalsResult result = service.getArrivals(stopId);

        switch (result.getStatus()) {
            case SUCCESS:
                List<BusInfo> buses = result.getBuses();
                if (buses.isEmpty()) {
                    System.out.println("No upcoming buses found.");
                } else {
                    for (BusInfo bus : buses) {
                        System.out.println(bus);
                    }
                }
                break;
            case FETCH_FAILED:
            case PARSE_FAILED:
                System.err.println(result.getMessage());
                System.exit(1);
                break;
            default:
                System.err.println("Unexpected result status.");
                System.exit(1);
        }
    }
}
