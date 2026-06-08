package com.example.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.ArrivalsResult;
import com.example.BusInfo;
import com.example.BusService;

/**
 * Tests GET /api/stops/{stopId}/arrivals without calling the real MTA API.
 * BusService is mocked so the test stays fast and does not need an API key.
 */
@WebMvcTest(BusController.class)
class BusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BusService busService;

    @Test
    void getArrivalsReturns200WithJsonArray() throws Exception {
        BusInfo bus = new BusInfo(
                "M15",
                "East Harlem",
                40.748,
                -73.985,
                "1234",
                "2026-05-03T12:00:00-04:00",
                2,
                "Example Stop");

        when(busService.getArrivals("404271")).thenReturn(ArrivalsResult.success(List.of(bus)));

        mockMvc.perform(get("/api/stops/404271/arrivals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lineRef").value("M15"))
                .andExpect(jsonPath("$[0].destinationName").value("East Harlem"))
                .andExpect(jsonPath("$[0].numStopsAway").value(2));
    }
}
