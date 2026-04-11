package com.example.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BusInfo;
import com.example.BusService;

@RestController
@RequestMapping("/api")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    // GET /api/stops/{stopId}/arrivals — same data as the CLI, as JSON.
    @GetMapping("/stops/{stopId}/arrivals")
    public List<BusInfo> arrivals(@PathVariable String stopId) {
        return busService.getIncomingBuses(stopId);
    }
}
