package com.example;

import java.util.Collections;
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

    public List<BusInfo> getIncomingBuses(String stopId){
        String json = fetcher.getBusTime(stopId);
        if (json == null || json.isEmpty()){
            System.err.println("No data received from API for stop " + stopId);
            return Collections.emptyList();
        }
        try {
            BusInfoParser parser = new BusInfoParser(json);
            JsonNode visits = parser.parseBusInfo();
            List<BusInfo> incomingBuses = parser.getStopVisits(visits);
            return incomingBuses;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        //return null;
        return Collections.emptyList();
    }
}
