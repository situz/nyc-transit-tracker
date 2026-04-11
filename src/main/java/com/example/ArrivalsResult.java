package com.example;

import java.util.Collections;
import java.util.List;

// Value object: one immutable snapshot of an arrivals request — success (maybe empty list) or a failure reason.
// final class so nobody subclasses it and breaks the intended invariants.
public final class ArrivalsResult {

    public enum Status {
        SUCCESS,
        FETCH_FAILED,
        PARSE_FAILED
    }

    // final fields: set once in the constructor, never reassigned — makes each result immutable and safe to pass around.
    // private: only this class and the static factories construct instances; callers use getters and Status.
    private final Status status;
    private final List<BusInfo> buses;
    private final String message;

    private ArrivalsResult(Status status, List<BusInfo> buses, String message) {
        this.status = status;
        this.buses = buses;
        this.message = message;
    }

    public static ArrivalsResult success(List<BusInfo> buses) {
        return new ArrivalsResult(Status.SUCCESS, buses, null);
    }

    public static ArrivalsResult fetchFailed(String message) {
        return new ArrivalsResult(Status.FETCH_FAILED, Collections.emptyList(), message);
    }

    public static ArrivalsResult parseFailed(String message) {
        return new ArrivalsResult(Status.PARSE_FAILED, Collections.emptyList(), message);
    }

    public Status getStatus() {
        return status;
    }

    // Only meaningful when status is SUCCESS; otherwise the list is empty (see factories).
    public List<BusInfo> getBuses() {
        return buses;
    }

    // Only set for FETCH_FAILED / PARSE_FAILED; null on SUCCESS.
    public String getMessage() {
        return message;
    }
}
