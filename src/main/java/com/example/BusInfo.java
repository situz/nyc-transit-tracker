package com.example;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusInfo {
    private String lineRef;
    private String destinationName;

    private double latitude;
    private double longitude;

    private String vehicleRef;

    private String expectedArrivalIso;
    private int numStopsAway;
    private String stopPointName;

    public BusInfo(){}

    public BusInfo(String lineRef, String destinationName, double latitude, double longitude, String vehicleRef,
            String expectedArrivalIso, int numStopsAway, String stopPointName) {
        this.lineRef = lineRef;
        this.destinationName = destinationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vehicleRef = vehicleRef;
        this.expectedArrivalIso = expectedArrivalIso;
        this.numStopsAway = numStopsAway;
        this.stopPointName = stopPointName;
    }



    public String getLineRef() {
        return lineRef;
    }

    public void setLineRef(String lineRef) {
        this.lineRef = lineRef;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getVehicleRef() {
        return vehicleRef;
    }

    public void setVehicleRef(String vehicleRef) {
        this.vehicleRef = vehicleRef;
    }

    public String getExpectedArrivalIso() {
        return expectedArrivalIso;
    }

    public void setExpectedArrivalIso(String expectedArrivalIso) {
        this.expectedArrivalIso = expectedArrivalIso;
    }

    public int getNumStopsAway() {
        return numStopsAway;
    }

    public void setNumStopsAway(int numStopsAway) {
        this.numStopsAway = numStopsAway;
    }

    public String getStopPointName() {
        return stopPointName;
    }

    public void setStopPointName(String stopPointName) {
        this.stopPointName = stopPointName;
    }

    public Long getMinutesUntilArrival() {
        if (expectedArrivalIso == null || expectedArrivalIso.isEmpty()) {
            return null;
        }
        try {
            OffsetDateTime expected = OffsetDateTime.parse(expectedArrivalIso);
            return Duration.between(OffsetDateTime.now(), expected).toMinutes();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        if (expectedArrivalIso == null || expectedArrivalIso.isEmpty()){
            return "BusInfo [lineRef=" + lineRef + ", destinationName=" + destinationName + ", latitude=" + latitude
                + ", longitude=" + longitude + ", vehicleRef=" + vehicleRef + ", expectedArrivalIso="
                + expectedArrivalIso + ", numStopsAway=" + numStopsAway + ", stopPointName=" + stopPointName + ", Time=N/A]\n";
        }
        OffsetDateTime expected;
        try {
            expected = OffsetDateTime.parse(expectedArrivalIso);
        } catch (DateTimeParseException e) {
            // If the API ever returns a non-ISO timestamp, don't crash when printing.
            return "BusInfo [lineRef=" + lineRef + ", destinationName=" + destinationName + ", latitude=" + latitude
                + ", longitude=" + longitude + ", vehicleRef=" + vehicleRef + ", expectedArrivalIso="
                + expectedArrivalIso + ", numStopsAway=" + numStopsAway + ", stopPointName=" + stopPointName + ", Time=N/A]\n";
        }
        OffsetDateTime now = OffsetDateTime.now();
        Duration arrival = Duration.between(now, expected);
        return "BusInfo [lineRef=" + lineRef + ", destinationName=" + destinationName + ", latitude=" + latitude
                + ", longitude=" + longitude + ", vehicleRef=" + vehicleRef + ", expectedArrivalIso="
                + expectedArrivalIso + ", numStopsAway=" + numStopsAway + ", stopPointName=" + stopPointName + ", Time="+ arrival.toMinutes() + "]\n";
    }

}
