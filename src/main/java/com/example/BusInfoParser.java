package com.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BusInfoParser {
    private String json;
    private JsonNode root;

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    } 

    public JsonNode getRoot() {
        return root;
    }

    public void setRoot(JsonNode root) {
        this.root = root;
    }

    // Parse the raw JSON string into a Jackson tree so we can navigate it via paths.
    public BusInfoParser(String json) throws JsonMappingException, JsonProcessingException {
        this.json = json;
        this.root = new ObjectMapper().readTree(json);
    }

    // Return the array of MonitoredStopVisit objects inside the SIRI envelope.
    public JsonNode parseBusInfo() throws JsonMappingException, JsonProcessingException{
        return root.path("Siri")
                    .path("ServiceDelivery")
                    .path("StopMonitoringDelivery")
                    .path(0)
                    .path("MonitoredStopVisit");
    }

    // Convert each MonitoredStopVisit entry into a BusInfo model object.
    public List<BusInfo> getStopVisits(JsonNode visits){
        List<BusInfo> incomingBuses = new ArrayList<BusInfo>();
        for (JsonNode visit : visits){
            BusInfo bus = new BusInfo();
            JsonNode journey = visit.path("MonitoredVehicleJourney");
            bus.setLineRef(journey.path("LineRef").asText());
            bus.setDestinationName(journey.path("DestinationName").asText());
            bus.setLatitude(journey.path("VehicleLocation").path("Latitude").asDouble());
            bus.setLongitude(journey.path("VehicleLocation").path("Longitude").asDouble());
            bus.setVehicleRef(journey.path("VehicleRef").asText());
            bus.setExpectedArrivalIso(journey.path("MonitoredCall").path("ExpectedArrivalTime").asText());
            bus.setNumStopsAway(journey.path("MonitoredCall").path("Extensions").path("Distances").path("StopsFromCall").asInt());
            bus.setStopPointName(journey.path("MonitoredCall").path("StopPointName").asText());
            incomingBuses.add(bus);
        }
        //System.out.println(incomingBuses.size());
        //System.out.println(visits.size());
        return incomingBuses;
    }

    // Debug helper: recursively print the JSON tree (objects, arrays, and values).
    public static void traverse(JsonNode node) {
        if (node.isObject()) {
            System.out.println("Object encountered.");
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                System.out.println("  Field: " + fieldName);
                traverse(node.get(fieldName)); // Recurse for field value
            }
        } else if (node.isArray()) {
            System.out.println("Array encountered.");
            for (int i = 0; i < node.size(); i++) {
                System.out.println("  Element " + i + ":");
                traverse(node.get(i)); // Recurse for array element
            }
        } else if (node.isValueNode()) {
            System.out.println("  Value: " + node.asText()); // Or node.asInt(), node.asBoolean(), etc.
        }
    }
}
