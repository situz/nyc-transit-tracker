package com.example;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class BusService {
    private MTAFetcher fetcher;
    
    public BusService(){
        this.fetcher = new MTAFetcher();
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
