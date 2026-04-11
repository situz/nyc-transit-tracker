package com.example;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class BusService {
    private final MTAFetcher fetcher;

    public BusService(MTAFetcher fetcher) {
        this.fetcher = fetcher;
    }

    /**
     * Fetches and parses MTA stop-monitoring JSON. Distinguishes upstream/parse errors from an empty schedule.
     */
    public ArrivalsResult getArrivals(String stopId) {
        String json = fetcher.getBusTime(stopId);
        if (json == null || json.isEmpty()) {
            return ArrivalsResult.fetchFailed(
                    "Could not fetch data from the MTA API for stop " + stopId + ".");
        }
        try {
            BusInfoParser parser = new BusInfoParser(json);
            JsonNode visits = parser.parseBusInfo();
            List<BusInfo> incomingBuses = parser.getStopVisits(visits);
            return ArrivalsResult.success(incomingBuses);
        } catch (JsonProcessingException e) {
            return ArrivalsResult.parseFailed(
                    "Could not parse API response: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }
    }
}
