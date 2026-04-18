package com.example.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ArrivalsResult;
import com.example.BusService;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api")
public class BusController {

    // final + constructor injection: Spring supplies one BusService; the reference never changes after construction.
    private final BusService busService;
    @Autowired
    public BusController(BusService busService) {
        this.busService = busService;
    }

    // GET /api/stops/{stopId}/arrivals — 200 + JSON array on success (may be []); 502/500 on errors.
    // ResponseEntity<?> means "HTTP response whose body type varies": List<BusInfo> on 200, Map on errors.
    // The wildcard is required because Java cannot name a single type that is both List and Map; callers rely on status code + JSON shape.
    @GetMapping("/stops/{stopId}/arrivals")
    public ResponseEntity<?> arrivals(@PathVariable String stopId) {
        ArrivalsResult result = busService.getArrivals(stopId);
        switch (result.getStatus()) {
            case SUCCESS:
                return ResponseEntity.ok(result.getBuses());
            case FETCH_FAILED:
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body(Map.of("error", result.getMessage()));
            case PARSE_FAILED:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.getMessage()));
            default:
                throw new IllegalStateException("Unhandled status: " + result.getStatus());
        }
    }
}
